package org.almuminune.devops.model.acme;

public enum AcmeUrl {
    // https://acme-v02.api.letsencrypt.org
    NONCE("https://acme-staging-v02.api.letsencrypt.org/acme/new-nonce"),
    ACCOUNT("https://acme-staging-v02.api.letsencrypt.org/acme/new-acct"),
    ORDER("https://acme-staging-v02.api.letsencrypt.org/acme/new-order");
    AcmeUrl (String url) {
        this.url = url;
    }
    private String url;
    public String get () {
        return this.url;
    }
}
