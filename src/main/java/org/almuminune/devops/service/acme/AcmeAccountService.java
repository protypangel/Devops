package org.almuminune.devops.service.acme;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import org.almuminune.devops.data.AcmeAccountData;
import org.almuminune.devops.model.acme.AcmeAccountResponseModel;
import org.almuminune.devops.model.acme.AcmeUrl;
import org.almuminune.devops.repository.AcmeAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@Service
public class AcmeAccountService {
    private final AcmeAccountRepository repository;
    @Getter private AcmeAccountData accountData;
    private Base64.Encoder encoder;
    private Signature signature;

    @Autowired private Supplier<String> nonce;

    @SneakyThrows
    public AcmeAccountService(@Autowired AcmeAccountRepository repository, @Autowired Supplier<String> nonce, @Autowired Base64.Encoder encoder, @Autowired Signature signature) {
        this.repository = repository;
        this.nonce = nonce;
        this.signature = signature;
        this.encoder = encoder;
        try {
            this.accountData = repository.findAll().stream().findFirst().orElseThrow();
            signature.initSign(this.accountData.getKeyPair().getPrivate());
        } catch (NoSuchElementException e) {
            CreateAccount();
        }
    }
    @SneakyThrows
    private void CreateAccount() {
        // Generate a key
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        this.accountData = new AcmeAccountData(keyPairGenerator.generateKeyPair());
        signature.initSign(this.accountData.getKeyPair().getPrivate());

        var response = CreateAccountExchange();
        // Get URL ID
        this.accountData.setIDAcct(
            response.getHeaders().get("Location").getFirst()
        );
        this.accountData.setKeyAuthorization(response.getBody().getKey());
        //repository.save(this.accountData);
    }
    private ResponseEntity<AcmeAccountResponseModel> CreateAccountExchange () {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/jose+json"));
        return new RestTemplate().exchange(
            new RequestEntity<>(
                GetBodyAccountExchange(),
                headers,
                HttpMethod.POST,
                java.net.URI.create(
                    AcmeUrl.ACCOUNT.get()
                )
            ),
            AcmeAccountResponseModel.class
        );
    }
    private String GetBodyAccountExchange() {
        JsonObject toBeReturned = new JsonObject();
        // JWK
        JsonObject jwk = new JsonObject();
        jwk.addProperty("kty", "RSA");
        jwk.addProperty("e", encoder.encodeToString(this.accountData.getPubKey_e_bytes()));
        jwk.addProperty("n", encoder.encodeToString(this.accountData.getPubKey_n_bytes()));
        // JWK -> PROTECTED
        JsonObject protector = new JsonObject();
        protector.add("jwk", jwk);
        protector.addProperty("url", AcmeUrl.ACCOUNT.get());
        protector.addProperty("nonce", nonce.get());
        protector.addProperty("alg", "RS256");
        String encodedProtector = encoder.encodeToString(protector.toString().getBytes());
        toBeReturned.addProperty(
            "protected",
            encodedProtector
        );

        // Payload
        JsonObject payload = new JsonObject();
        payload.addProperty("termsOfServiceAgreed", true);
        String encodedPayload = encoder.encodeToString(payload.toString().getBytes());
        toBeReturned.addProperty(
            "payload",
            encodedPayload
        );
        // Signature
        toBeReturned.addProperty(
            "signature",
            getEncodedSignature(
                this.getAccountData().getKeyPair(),
                encodedProtector + "." + encodedPayload
            )
        );

        return toBeReturned.toString();
    }
    @SneakyThrows
    public String getEncodedSignature (KeyPair keyPair, String encodedProtector) {
        signature.initSign(keyPair.getPrivate());
        signature.update(encodedProtector.getBytes());
        byte[] signatureBytes = signature.sign();
        return encoder.encodeToString(signatureBytes);
    }
}
