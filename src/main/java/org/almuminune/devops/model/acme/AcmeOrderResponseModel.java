package org.almuminune.devops.model.acme;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class AcmeOrderResponseModel {
    private List<String> authorizations;
    private String finalize;
    private String status;
}
