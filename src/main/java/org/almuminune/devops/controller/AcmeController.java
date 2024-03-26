package org.almuminune.devops.controller;

import org.almuminune.devops.service.acme.challenge.AcmeChallenge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(".well-known/acme-challenge/")
public class AcmeController {
    @Autowired AcmeChallenge challenge;
    @GetMapping("{token}")
    public String challenge (@PathVariable String token, @RequestHeader("Host") String host, @RequestHeader Map<String, String> headers) {
        System.out.println("token = " + token + ", host = " + host);
        String tokenFromHost = challenge.getTokenFromHost(host);
        boolean isCorrect = tokenFromHost.equals(token);
        return token + "." + tokenFromHost;
    }
}
