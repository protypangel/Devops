package org.almuminune.devops.configuration;

import lombok.SneakyThrows;
import org.almuminune.devops.data.AcmeAccountData;
import org.almuminune.devops.model.acme.AcmeAccountRequestModel;
import org.almuminune.devops.model.acme.AcmeAccountResponseModel;
import org.almuminune.devops.model.acme.AcmeUrl;
import org.almuminune.devops.repository.AcmeAccountRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.Signature;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@Configuration
public class AcmeConfiguration {
    private Encoder encoder;
    private Signature signature;
    @Autowired RestTemplate restTemplate;
    private Supplier<AcmeAccountData> supplier;
    private Supplier<String> createNonce;
    @SneakyThrows
    public AcmeConfiguration(@Autowired AcmeAccountRepository repository) {
        encoder = Base64.getUrlEncoder().withoutPadding();
        signature = Signature.getInstance("SHA256withRSA");
        Security.addProvider(new BouncyCastleProvider());
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
        supplier = () -> {
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
            supplier = () -> finalAcmeAccountData;
            return acmeAccountData;
        };
    }
    @SneakyThrows
    private AcmeAccountData CreateAccount() {
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
                AcmeAccountRequestModel.CreateAccount(encoder, signature, accountData, createNonce.get(), accountData.getKeyPair()),
                headers,
                HttpMethod.POST,
                URI.create(
                    AcmeUrl.ACCOUNT.get()
                )
            ),
            AcmeAccountResponseModel.class
        );

        accountData.setIDAcct(
            response.getHeaders().get("Location").getFirst()
        );
        accountData.setKeyAuthorization(response.getBody().getKey());

        return accountData;
    }

    @Bean(name = "encoder") public Encoder getEncoder () {
        return encoder;
    }
    @Bean(name = "signature") public Signature getSignature () {
        return signature;
    }
    @Bean(name = "nonce") public Supplier<String> createNonce () {
        return createNonce;
    }
    @Bean(name = "account") public AcmeAccountData getAccount() {
        return supplier.get();
    }
}
