package org.almuminune.devops.configuration;

import org.almuminune.devops.service.AcmeService;
import org.almuminune.devops.service.InterceptorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {
    InterceptorService service;
    public InterceptorConfiguration (@Autowired AcmeService service) {
        this.service = new InterceptorService(service);
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(service);
    }
}