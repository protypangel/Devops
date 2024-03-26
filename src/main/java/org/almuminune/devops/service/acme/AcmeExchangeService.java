package org.almuminune.devops.service.acme;

import org.almuminune.devops.model.acme.AcmeUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AcmeExchangeService {
    @Autowired private RestTemplate restTemplate;
    @Autowired private AcmeBodyTemplate acmeBodyTemplate;

    public ResponseEntity<String> CreateOrder(String ...url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/jose+json"));
        String body = acmeBodyTemplate.CreateOrder(url);
        return restTemplate.exchange(
            new RequestEntity<>(
                body,
                headers,
                HttpMethod.POST,
                java.net.URI.create(
                    AcmeUrl.ORDER.get()
                )
            ),
            String.class
        );
    }
    public ResponseEntity<String> getChallenges (String urlChallenge) {
        var requestEntity = new RequestEntity<>(
            new HttpHeaders(),
            HttpMethod.GET,
            java.net.URI.create(
                urlChallenge
            )
        );
        return restTemplate.exchange(requestEntity, String.class);
    }

}
