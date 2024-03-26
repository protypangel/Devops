package org.almuminune.devops.service.acme;

import com.google.gson.JsonElement;
import org.almuminune.devops.service.acme.challenge.AcmeChallenge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.StreamSupport;

import static com.google.gson.JsonParser.parseString;

@Service
public class AcmeService {
    @Autowired private AcmeChallenge acmeChallenge;
    @Autowired private RestTemplate restTemplate;
    @Autowired private AcmeExchangeService acmeExchangeService;
    @Autowired private AcmeAccountService acmeAccountService;
    public void CreateACertificate(String ...url) {
        var order = acmeExchangeService.CreateOrder(url);
        // Get Authorizations and finalize
        var json = parseString(order.getBody()).getAsJsonObject();
        var authorizations = StreamSupport.stream(json.get("authorizations").getAsJsonArray().spliterator(), false).map(JsonElement::getAsString).toList();
        var finalize = json.get("finalize").getAsString();

        for (var challenge : authorizations)
            challenge(challenge);

//        FinalizeOrder(
//            order.getValue1(),
//            acmeModel.FinalizeOrder(
//                keys.getValue2(),
//                account.getValue2(),
//                order.getValue1(),
//                CreateNonce(),
//                url
//            )
//        );
    }

    private void challenge(String challenge) {

    }

    private void FinalizeOrder(String finalizeUrl, String model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/jose+json"));

        var requestEntity = new RequestEntity<>(
            model,
            headers,
            HttpMethod.POST,
            java.net.URI.create(
                finalizeUrl
            )
        );
        var response = restTemplate.exchange(requestEntity, String.class);
        return;
    }
}
