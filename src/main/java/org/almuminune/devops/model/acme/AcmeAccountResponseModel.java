package org.almuminune.devops.model.acme;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
public class AcmeAccountResponseModel {
    private String n, e, key;
    private static ObjectMapper objectMapper = new ObjectMapper();
    // Constructeur prenant un Map<String, String> comme argument et initialisant la variable kty
    @SneakyThrows
    public void setKey (Map<String, String> key) {
        this.n = key.get("n");
        this.e = key.get("e");
        String k = objectMapper.writeValueAsString(key);
        this.key = RSAKey.parse(k).computeThumbprint().toString();
    }
}
