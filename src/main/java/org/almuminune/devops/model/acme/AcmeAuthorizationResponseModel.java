package org.almuminune.devops.model.acme;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter // TODO: Test
public class AcmeAuthorizationResponseModel {
    private AcmeAuthorizationChallengeResponseModel http, dns, tlsAlpn;
    private String url;
    public void setIdentifier(Map<String, String> identifier) {
        url = identifier.get("value");
    }
    public void setChallenges(List<Map<String, Object>> challenges) {
        challenges.forEach(challenge -> {
            switch ((String) challenge.get("type")) {
                case "http-01":
                    http = new AcmeAuthorizationChallengeResponseModel(challenge);
                    break;
                case "dns-01":
                    dns = new AcmeAuthorizationChallengeResponseModel(challenge);
                    break;
                case "tls-alpn-01":
                    tlsAlpn = new AcmeAuthorizationChallengeResponseModel(challenge);
                    break;
            }
        });
    }
    @Getter
    @ToString
    @AllArgsConstructor // TODO: Test
    public static class AcmeAuthorizationChallengeResponseModel {
        String status, url, token;
        public AcmeAuthorizationChallengeResponseModel(Map<String, Object> challenge) {
            status = (String) challenge.get("status");
            url = (String) challenge.get("url");
            token = (String) challenge.get("token");
        }
    }
}
