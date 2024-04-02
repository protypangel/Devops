package org.almuminune.devops.configuration;

import org.almuminune.devops.data.AcmeAccountData;
import org.almuminune.devops.repository.AcmeAccountRepository;
import org.almuminune.devops.model.acme.AcmeRequestModelBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.security.Security;
import java.security.Signature;
import java.util.Base64.Encoder;
import java.util.function.Supplier;

@Configuration
public class AcmeConfiguration extends AcmeRequestModelBuilder {
    public AcmeConfiguration(@Autowired AcmeAccountRepository repository, @Autowired RestTemplate restTemplate) {
        super(repository, restTemplate);
        Security.addProvider(new BouncyCastleProvider());
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
        return account.get();
    }
}
