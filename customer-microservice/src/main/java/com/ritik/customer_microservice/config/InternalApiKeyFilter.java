package com.ritik.customer_microservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
@Slf4j
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${internal.api.key}")
    private String expectedKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip non-internal endpoints
        if (!path.startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Processing internal API request | path={}", path);

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null) {
            log.warn("Missing internal API key | path={}", path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!expectedKey.equals(apiKey)) {
            log.warn("Invalid internal API key | path={}", path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        log.info("Internal API key validated successfully | path={}", path);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "BANK_MICROSERVICE", null,
                List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
