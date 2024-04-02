package org.almuminune.devops.model.acme;

import lombok.SneakyThrows;
import org.almuminune.devops.data.AcmeAccountData;
import org.almuminune.devops.repository.AcmeAccountRepository;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.shredzone.acme4j.Identifier;
import org.shredzone.acme4j.toolbox.AcmeUtils;
import org.shredzone.acme4j.util.CSRBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.x500.X500Principal;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class AcmeRequestModelBuilder {
    protected Base64.Encoder encoder;
    protected Signature signature;
    protected Supplier<String> createNonce;
    protected Supplier<AcmeAccountData> account;
    protected RestTemplate restTemplate;
    // FOR AcmeExchangeService
    public AcmeRequestModelBuilder (AcmeRequestModelBuilder builder) {
        this.encoder = builder.encoder;
        this.signature = builder.signature;
        this.createNonce = builder.createNonce;
        this.account = builder.account;
        this.restTemplate = builder.restTemplate;
    }
    // FOR AcmeConfiguration
    @SneakyThrows
    public AcmeRequestModelBuilder(AcmeAccountRepository repository, RestTemplate restTemplate) {
        encoder = Base64.getUrlEncoder().withoutPadding();
        signature = Signature.getInstance("SHA256withRSA");
        this.restTemplate = restTemplate;
        // Set createNonce
        HttpHeaders headers = new HttpHeaders();
        var requestEntity = new RequestEntity<>(
            headers,
            HttpMethod.HEAD,
            URI.create(
                AcmeUrl.NONCE.get()
            )
        );
        createNonce = () -> restTemplate.exchange(requestEntity, Void.class)
            .getHeaders().get("Replay-Nonce").getFirst();
        // Set acmeSupplier
        account = () -> {
            AcmeAccountData acmeAccountData = null;
            try {
                acmeAccountData = repository.findAll().stream().findFirst().orElseThrow();
                signature.initSign(acmeAccountData.getKeyPair().getPrivate());
            } catch (NoSuchElementException e) {
                acmeAccountData = CreateAccount();
                repository.save(acmeAccountData);
            } catch (InvalidKeyException e) {

            }
            AcmeAccountData finalAcmeAccountData = acmeAccountData;
            account = () -> finalAcmeAccountData;
            return acmeAccountData;
        };
    }
    @SneakyThrows
    protected AcmeAccountData CreateAccount() {
        // Generate a key
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        AcmeAccountData accountData = new AcmeAccountData(keyPairGenerator.generateKeyPair());
        signature.initSign(accountData.getKeyPair().getPrivate());

        // Call Acme
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/jose+json"));
        var response = restTemplate.exchange(
            new RequestEntity<>(
                CreateAccountRequestModel(accountData),
                headers,
                HttpMethod.POST,
                URI.create(
                    AcmeUrl.ACCOUNT.get()
                )
            ),
            AcmeAccountResponseModel.class
        );

        accountData.setIDAcct(
            Objects.requireNonNull(response.getHeaders().get("Location")).getFirst()
        );
        accountData.setKeyAuthorization(Objects.requireNonNull(response.getBody()).getKey());

        return accountData;
    }
    private String CreateAccountRequestModel (AcmeAccountData acmeAccountData) {
        String protector = """
            {
                "jwk": {
                    "kty": "RSA",
                    "e": "%s",
                    "n": "%s"
                },
                "url": "%s",
                "nonce": "%s",
                "alg": "RS256"
            }
            """.formatted(
            encoder.encodeToString(acmeAccountData.getPubKey_e_bytes()),
            encoder.encodeToString(acmeAccountData.getPubKey_n_bytes()),
            AcmeUrl.ACCOUNT.get(),
            createNonce.get()
        );
        String payload = """
            {
                "termsOfServiceAgreed": true
            }
            """;
        return getAcmeRequestModel(protector, payload);
    }
    protected String CreateOrderRequestModel (String ...url) {
        String protector = """
            {
                "url": "%s",
                "nonce": "%s",
                "kid": "%s",
                "alg": "RS256"
            }
            """.formatted(
            AcmeUrl.ORDER.get(),
            createNonce.get(),
            account.get().getIDAcct()
        );
        String payload = """
            {
                "identifiers": [
                    %s
                ]
            }
            """.formatted(
                Stream.of(url).map(
                    """
                    {
                    \t\t\t"type": "dns",
                    \t\t\t"value": "%s"
                    \t\t}"""::formatted
                ).collect(Collectors.joining(","))
            );
        return getAcmeRequestModel(protector, payload);
    }
    protected String CreateChallengeRequestModel(String url) {
        String protector = """
            {
                "url": "%s",
                "nonce": "%s",
                "kid": "%s",
                "alg": "RS256"
            }
            """.formatted(
            url,
            createNonce.get(),
            account.get().getIDAcct()
        );
        String payload = """
            {
                "keyAuthorization": "%s"
            }
            """.formatted(account.get().getKeyAuthorization());
        return getAcmeRequestModel(protector, payload);
    }
    @SneakyThrows
    protected String FinalizeRequestModel(String finalizeUrl, String url) {
        String protector = """
        {
            "url": "%s",
            "nonce": "%s",
            "kid": "%s",
            "alg": "RS256"
        }
        """.formatted(
            finalizeUrl,
            createNonce.get(),
            account.get().getIDAcct()
        );
        KeyPair keyPair = account.get().getKeyPair();
        StringBuilder sb = new StringBuilder("C=France, ST=IleDeFrance, L=Paris, O=Almuminune OU=AlmuminuneDev CN=").append(url);
        X500Principal subject = new X500Principal(sb.toString());
        // Créer le constructeur de la requête de certification
        PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(
            "SHA1withRSA",
            subject,
            keyPair.getPublic(),
            null,
            keyPair.getPrivate()
        );
        // Lire le contenu du fichier DER
//        byte[] derBytes = Files.readAllBytes(Paths.get("request.der"));
//
//        // Convertir les bytes en une chaîne hexadécimale
//        StringBuilder stringBuilder = new StringBuilder();
//        for (byte b : derBytes) {
//            stringBuilder.append(String.format("%02X", b));
//        }
//        String derString = stringBuilder.toString();

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(Identifier.dns(url));

        CSRBuilder csrBuilder = new CSRBuilder();
        csrBuilder.addIdentifiers(identifiers);
        csrBuilder.sign(keyPair);

        return getAcmeRequestModel2(protector, csrBuilder.getEncoded());
    }

    @SneakyThrows
    private String getAcmeRequestModel(String protector, String payload) {
        String protectorEncoder = encoder.encodeToString(protector.getBytes());
        String payloadEncoder = encoder.encodeToString(payload.getBytes());
        signature.update((protectorEncoder + "." + payloadEncoder).getBytes());
        return """
        {
            "protected": "%s",
            "payload": "%s",
            "signature": "%s"
        }
        """.formatted(protectorEncoder, payloadEncoder, encoder.encodeToString(signature.sign()));
    }
    @SneakyThrows
    private String getAcmeRequestModel2(String protector, byte[] payload) {
        String protectorEncoder = encoder.encodeToString(protector.getBytes());
        String payloadEncoder = encoder.encodeToString(payload);
        signature.update((protectorEncoder + "." + payloadEncoder).getBytes());
        return """
        {
            "protected": "%s",
            "payload": {
                "csr": "%s"
            },
            "signature": "%s"
        }
        """.formatted(protectorEncoder, AcmeUtils.base64UrlEncode(payload), encoder.encodeToString(signature.sign()));
    }

}
