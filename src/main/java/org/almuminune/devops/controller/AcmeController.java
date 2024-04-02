package org.almuminune.devops.controller;

import org.almuminune.devops.service.AcmeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(".well-known/acme-challenge/")
public class AcmeController {
    @Autowired AcmeService service;
    @GetMapping("{token}")
    public ResponseEntity<?> challenge (
        @PathVariable String token,
        @RequestHeader("Host") String host
    ) {
        if (!service.tokens.containsKey(host)) return ResponseEntity.notFound().build();
        if (!service.tokens.get(host).getHttp().getToken().equals(token)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(token + "." + service.getAuthorizationAccount());
    }
}
