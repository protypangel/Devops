package org.almuminune.devops.service.acme.challenge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import org.almuminune.devops.service.acme.AcmeBodyTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.gson.JsonParser.parseString;

@Service
public class AcmeChallenge {
    @Autowired private RestTemplate restTemplate;
    @Autowired private AcmeBodyTemplate acmeBodyTemplate;
    public ConcurrentHashMap<String, AcmeChallengeState> states;

    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    public void Challenge(KeyPair keyPair, String kid, String challengeUrl, String keyAccount) {
        System.out.println(challengeUrl);
        HttpHeaders headers = new HttpHeaders();

        var requestEntity = new RequestEntity<>(
            headers,
            HttpMethod.GET,
            java.net.URI.create(
                challengeUrl
            )
        );
        var response = restTemplate.exchange(requestEntity, String.class);
        JsonObject json =  parseString(response.getBody()).getAsJsonObject();
        System.out.println("body: "+ response.getBody());
        String token = json.get("challenges").getAsJsonArray().get(0).getAsJsonObject().get("token").getAsString();
        String identifierUrl = json.get("identifier").getAsJsonObject().get("value").getAsString();
        map.put(identifierUrl, token);

        // TEST
        try {
            RSAKey rsaKey = RSAKey.parse(keyAccount);
            Base64URL url = rsaKey.computeThumbprint();
            System.err.println("url.toString() = " + url.toString());
        } catch (Exception e) {

        }

        // FIN TEST

        for (JsonElement challenge : json.get("challenges").getAsJsonArray()) {
            JsonObject object = challenge.getAsJsonObject();
            String url = object.get("url").getAsString();
            switch (object.get("type").getAsString()) {
                case "http-01":
                    httpChallenge(keyPair, kid, url, token);
                    break;
                case "dns-01":
                    dnsChallenge(keyPair, kid, url, token);
                    break;
                case "tls-alpn-01":
                    break;
                default:
                    System.out.println("object.get(\"type\").getAsString() = " + object.get("type").getAsString());
                    break;
            }
       }
    }

    private void dnsChallenge(KeyPair keyPair, String kid, String url, String token) {

    }

    public String getTokenFromHost(String host) {
        return map.get(host);
    }
    private void httpChallenge (KeyPair keyPair, String kid, String url, String token) {
//        String model = acmeBodyTemplate.httpChallenge(acmeNonce.Create(), keyPair, kid, url, token);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType("application/jose+json"));
//
//        var requestEntity = new RequestEntity<>(
//            model,
//            headers,
//            HttpMethod.POST,
//            java.net.URI.create(
//                url
//            )
//        );
//        var response = restTemplate.exchange(requestEntity, String.class);
//        System.out.println("http response = " + response);
    }
    public void challengeIsClose() {

    }
}
