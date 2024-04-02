package org.almuminune.devops;

import org.almuminune.devops.repository.AcmeAccountRepository;
import org.almuminune.devops.service.AcmeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class AcmeClientApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(AcmeClientApplication.class, args);
    }

    @Autowired AcmeService service;
    @Autowired RestTemplate restTemplate;
    @Autowired
    AcmeAccountRepository repository;

    public void run(String... args) {
        service.CreateACertificate(
        //    "almuminune.org",
            "al-muminune.org"
        );
    }
}
