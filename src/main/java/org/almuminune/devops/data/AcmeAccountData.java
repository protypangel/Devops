package org.almuminune.devops.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Entity(name = "acme")
@Getter
@NoArgsConstructor
public class AcmeAccountData {
    public AcmeAccountData(
        KeyPair keyPair
    ) {
        this.publicKeyBytes = keyPair.getPublic().getEncoded();
        this.privateKeyBytes = keyPair.getPrivate().getEncoded();
        this.keyPair = keyPair;
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        this.pubKey_e_bytes = rsaPublicKey.getPublicExponent().toByteArray();
        this.pubKey_n_bytes = rsaPublicKey.getModulus().toByteArray();
    }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob private byte[] publicKeyBytes;
    @Lob private byte[] privateKeyBytes;
    @Lob private byte[] pubKey_e_bytes;
    @Lob private byte[] pubKey_n_bytes;
    @Setter private String IDAcct;
    @Setter private String keyAuthorization;
    @Transient private KeyPair keyPair;

    @PostLoad
    @SneakyThrows
    public void init () {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(
            new X509EncodedKeySpec(
                publicKeyBytes
            )
        );
        PrivateKey privateKey = keyFactory.generatePrivate(
            new PKCS8EncodedKeySpec(
                privateKeyBytes
            )
        );
        this.keyPair = new KeyPair(publicKey, privateKey);
    }
}
