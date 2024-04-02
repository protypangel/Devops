package org.almuminune.devops.service;

import lombok.SneakyThrows;
import org.almuminune.devops.configuration.AcmeConfiguration;
import org.almuminune.devops.model.acme.AcmeAuthorizationResponseModel;
import org.almuminune.devops.model.acme.AcmeOrderResponseModel;
import org.almuminune.devops.model.acme.AcmeUrl;
import org.almuminune.devops.model.acme.AcmeRequestModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AcmeService extends AcmeRequestModelBuilder {
    public ConcurrentHashMap<String, AcmeAuthorizationResponseModel> tokens;
    public ConcurrentHashMap<String, String> authorizations;
    public ConcurrentHashMap<String, String> finalized;
    public ConcurrentHashMap<String, String[]> url;
    public AcmeService(@Autowired AcmeConfiguration configuration) {
        super(configuration);
        tokens = new ConcurrentHashMap<>();
        authorizations = new ConcurrentHashMap<>();
        finalized = new ConcurrentHashMap<>();
        url = new ConcurrentHashMap<>();
    }
    public String getAuthorizationAccount () {
        return account.get().getKeyAuthorization();
    }
    public ResponseEntity<AcmeOrderResponseModel> CreateOrder(String ...url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/jose+json"));
        String body = super.CreateOrderRequestModel(url);
        return restTemplate.exchange(
            new RequestEntity<>(
                body,
                headers,
                HttpMethod.POST,
                java.net.URI.create(
                    AcmeUrl.ORDER.get()
                )
            ),
            AcmeOrderResponseModel.class
        );
    }
    public void CreateACertificate(String ...url) {
        var order = CreateOrder(url);
        // Play all authorization
        for (var authorization : Objects.requireNonNull(order.getBody()).getAuthorizations()) {
            var response = authorization(authorization);
            System.out.println("response.getHttp() = " + response.getHttp());
            System.out.println("response.getDns() = " + response.getDns());
            System.out.println("response.getTlsAlpn() = " + response.getTlsAlpn());
            System.out.println("authorization = " + authorization);
            this.authorizations.put(response.getUrl(), authorization);
            this.tokens.put(response.getUrl(), response);
            this.finalized.put(response.getUrl(), order.getBody().getFinalize());
            this.url.put(response.getUrl(), url);
            if (checkAuthorization(response.getUrl()))
                callChallenge(response.getHttp().getUrl());
            //callChallenge(response.getDns().getUrl());
            //callChallenge(response.getTlsAlpn().getUrl());
        }
    }

    private void callChallenge(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/jose+json"));
        String body = super.CreateChallengeRequestModel(url);
        restTemplate.exchange(
            new RequestEntity<>(
                body,
                headers,
                HttpMethod.POST,
                java.net.URI.create(
                    url
                )
            ),
            AcmeOrderResponseModel.class
        );
    }

    private AcmeAuthorizationResponseModel authorization(String authorization) {
        var requestEntity = new RequestEntity<>(
            new HttpHeaders(),
            HttpMethod.GET,
            java.net.URI.create(
                authorization
            )
        );
        return restTemplate.exchange(requestEntity, AcmeAuthorizationResponseModel.class).getBody();
    }
    public boolean checkAuthorization (String host) {
        if (!authorizations.containsKey(host)) return false;
        var authorization = authorizations.get(host);
        var response = authorization(authorization);
        if (response.getHttp().getStatus().equals("pending")) return true;
        var token = tokens.remove(host);
        var finalize = finalized.remove(host);
        var url = this.url.remove(host);
        authorizations.remove(host);
        finishOrder(finalize, response.getHttp().getStatus(), host);
        return false;
    }

    @SneakyThrows
    private void finishOrder(String finishOrder, String status, String host) {
        if (!status.equals("valid")) throw new Exception("Status is incorrect : " + status);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/jose+json"));
        String body = super.FinalizeRequestModel(finishOrder, host);
        var json = restTemplate.exchange(
            new RequestEntity<>(
                body,
                headers,
                HttpMethod.POST,
                java.net.URI.create(
                    finishOrder
                )
            ),
            String.class
        );


        System.out.println(json);
    }
}
