package org.almuminune.devops.service.acme;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.almuminune.devops.configuration.AcmeConfiguration;
import org.almuminune.devops.model.acme.AcmeUrl;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.security.KeyPair;
import java.security.Signature;
import java.util.Base64.Encoder;
import java.util.function.Supplier;

@Service
public class AcmeBodyTemplate {
    Gson gson;
    @Autowired
    AcmeConfiguration configuration;
    // @Autowired private AcmeAccountService acmeAccountService;
    @Autowired private Supplier<String> createNonce;
    @SneakyThrows
    public AcmeBodyTemplate() {
        gson = new Gson();
    }
    public String CreateOrder(String ...url) {
        Encoder encoder = configuration.getEncoder();
        JsonObject toBeReturned = new JsonObject();
        // Protected
        JsonObject protector = new JsonObject();
        protector.addProperty("alg", "RS256");
//        protector.addProperty("kid", acmeAccountService.getAccountData().getIDAcct());
        protector.addProperty("nonce", createNonce.get());
        protector.addProperty("url", AcmeUrl.ORDER.get());
        String encodedProtector = encoder.encodeToString(protector.toString().getBytes());
        toBeReturned.addProperty(
            "protected",
            encodedProtector
        );
        // Payload
        JsonObject payload = new JsonObject();
        JsonArray identifiers = new JsonArray();
        for (String u : url)
            identifiers.add(CreateOrderIdentifier(u));

        payload.add("identifiers", identifiers);
        String encodedPayload = encoder.encodeToString(payload.toString().getBytes());
        toBeReturned.addProperty(
            "payload",
            encodedPayload
        );

        // Signature
        toBeReturned.addProperty(
            "signature",
            getEncodedSignature(
                null,
//                acmeAccountService.getAccountData().getKeyPair(),
                encodedProtector,
                encodedPayload
            )
        );

        return toBeReturned.toString();
    }
    private JsonObject CreateOrderIdentifier(String url) {
        JsonObject identifier = new JsonObject();
        identifier.addProperty("type", "dns");
        identifier.addProperty("value", url);
        return identifier;
    }
    @SneakyThrows
    public String FinalizeOrder(KeyPair keyPair, String kid, String finalizeUrl, String nonce, String ...url) {
        Encoder encoder = configuration.getEncoder();
        JsonObject toBeReturned = new JsonObject();
        // Protected
        JsonObject protector = new JsonObject();
        protector.addProperty("alg", "RS256");
        protector.addProperty("kid", kid);
        protector.addProperty("nonce", nonce);
        protector.addProperty("url", finalizeUrl);
        String encodedProtector = encoder.encodeToString(protector.toString().getBytes());
        toBeReturned.addProperty(
            "protected",
            encodedProtector
        );
        // Payload
        JsonObject payload = new JsonObject();
        StringBuilder sb = new StringBuilder("C=France, ST=IleDeFrance, L=Paris, OU=AlmuminuneDev, O=Almuminune CN=").append(url[0]);
        for (int i = 1; i < url.length; i++)
            sb.append(", DNS=").append(url[i]);
        X500Principal subject = new X500Principal(sb.toString());
        // Créer le constructeur de la requête de certification
        PKCS10CertificationRequest csr = new PKCS10CertificationRequest(
            "SHA1withRSA",
            subject,
            keyPair.getPublic(),
            null,
            keyPair.getPrivate()
        );
        payload.addProperty("csr", encoder.encodeToString(csr.getEncoded()));
        String encodedPayload = encoder.encodeToString(payload.toString().getBytes());
            toBeReturned.addProperty(
            "payload",
            encodedPayload
        );

        // Signature
        toBeReturned.addProperty(
            "signature",
            getEncodedSignature(
                keyPair,
                encodedProtector,
                encodedPayload
            )
        );

        return toBeReturned.toString();
    }
    public String httpChallenge(String nonce, KeyPair keyPair, String kid, String url, String token) {
        Encoder encoder = configuration.getEncoder();

        System.err.println("nonce:" + nonce);

        JsonObject toBeReturned = new JsonObject();
        // Protected
        JsonObject protector = new JsonObject();
        protector.addProperty("alg", "RS256");
        protector.addProperty("kid", kid);
        protector.addProperty("nonce", nonce);
        protector.addProperty("url", url);
        String encodedProtector = encoder.encodeToString(protector.toString().getBytes());
        toBeReturned.addProperty(
            "protected",
            encodedProtector
        );
        // Payload
        JsonObject payload = new JsonObject();
        System.err.println("token:" + token);
        payload.addProperty("keyAuthorization", token);
        String encodedPayload = encoder.encodeToString(payload.toString().getBytes());
        toBeReturned.addProperty(
            "payload",
            encodedPayload
        );

        // Signature
        toBeReturned.addProperty(
            "signature",
            getEncodedSignature(
                keyPair,
                encodedProtector,
                encodedPayload
            )
        );

        return toBeReturned.toString();
    }

    @SneakyThrows
    private String getEncodedSignature (KeyPair keyPair, String encodedProtector, String encodedPayload) {
        String toSign = encodedProtector + "." + encodedPayload;
        Signature signature = configuration.getSignature();
        Encoder encoder = configuration.getEncoder();
        signature.initSign(keyPair.getPrivate());
        signature.update(toSign.getBytes());
        byte[] signatureBytes = signature.sign();
        return encoder.encodeToString(signatureBytes);
    }
    @SneakyThrows
    public String getEncodedSignature (KeyPair keyPair, String encodedProtector) {
        Signature signature = configuration.getSignature();
        Encoder encoder = configuration.getEncoder();
        signature.initSign(keyPair.getPrivate());
        signature.update(encodedProtector.getBytes());
        byte[] signatureBytes = signature.sign();
        return encoder.encodeToString(signatureBytes);
    }
}
