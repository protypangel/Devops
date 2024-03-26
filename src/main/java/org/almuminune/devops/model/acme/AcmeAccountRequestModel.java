package org.almuminune.devops.model.acme;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.almuminune.devops.data.AcmeAccountData;

import java.security.KeyPair;
import java.security.Signature;
import java.util.Base64;


@NoArgsConstructor
public class AcmeAccountRequestModel {
    @JsonProperty("protected")
    private String protector;
    private String payload;
    private String signature;
    @SneakyThrows
    public AcmeAccountRequestModel (Base64.Encoder encoder, Signature signature, String protector, String payload) {
        this.protector = encoder.encodeToString(protector.getBytes());
        this.payload = encoder.encodeToString(payload.getBytes());
        signature.update((this.protector + "." + this.payload).getBytes());
        this.signature = encoder.encodeToString(signature.sign());
    }
    @SneakyThrows
    public static String CreateAccount (Base64.Encoder encoder, Signature signature, AcmeAccountData acmeAccountData, String nonce, KeyPair keyPair) {
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
                nonce
        );
        String payload = """
            {
                "termsOfServiceAgreed": true
            }
            """;
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
}
