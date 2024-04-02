package org.almuminune.devops.configuration;

import com.nimbusds.jose.shaded.gson.JsonObject;
import lombok.SneakyThrows;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.security.KeyPair;
import java.util.Base64.Encoder;
import java.util.function.Supplier;

@Service
public class AcmeBodyTemplate {
    AcmeConfiguration configuration;
    @Autowired private Supplier<String> createNonce;
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
            ""
//            getEncodedSignature(
//                keyPair,
//                encodedProtector,
//                encodedPayload
//            )
        );

        return toBeReturned.toString();
    }
}
