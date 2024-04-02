package org.almuminune.devops.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.almuminune.devops.controller.AcmeController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class InterceptorService implements HandlerInterceptor {
    AcmeService service;
    public InterceptorService(@Autowired AcmeService acmeService) {
        this.service = acmeService;
    }

    @SneakyThrows
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        if (! (handler instanceof HandlerMethod handling)) return;
        if (!(handling.getBean() instanceof AcmeController) || !handling.getMethod().getName().equals("challenge") || response.getStatus() != 200)
            return;
        boolean check;
        int sleep = 1;
        do {
            TimeUnit.SECONDS.sleep(sleep);
            check = service.checkAuthorization(request.getHeader("host"));
            sleep++;
        } while (check && sleep < 10);
    }
}