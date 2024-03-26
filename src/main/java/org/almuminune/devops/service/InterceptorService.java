package org.almuminune.devops.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class InterceptorService implements HandlerInterceptor {
    private int index = 0;
    private static int min = 0;
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        index++;
        if (index -1 < min) return true;
        // Imprimer toutes les informations utiles de la requête
        System.out.println("===============================================================================");
        System.out.println("URL appelée : " + request.getRequestURI());
        System.out.println("Méthode HTTP : " + request.getMethod());
        System.out.println("Adresse IP du client : " + request.getRemoteAddr());
        System.out.println("En-têtes de la requête :");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            System.out.println(headerName + ": " + request.getHeader(headerName));
        });
        System.out.println("Paramètres de la requête :");
        request.getParameterMap().forEach((paramName, paramValues) -> {
            System.out.println(paramName + ": " + String.join(", ", paramValues));
        });
        System.out.println("Type de contenu : " + request.getContentType());
        System.out.println("Adresse URL complète : " + request.getRequestURL());
        System.out.println("===============================================================================");
        return true;
    }
}