package com.ritik.customer_microservice.config;

import com.ritik.customer_microservice.exception.UnauthorizedException;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.model.CustomerSession;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.repository.CustomerSessionRepository;
import com.ritik.customer_microservice.serviceImpl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtServiceImpl jwtService;
    private final CustomerRepository customerRepository;
    private final UserDetailsService userDetailsService;
    private final CustomerSessionRepository customerSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.extractClaims(token);
            CustomerSession session = customerSessionRepository.findByToken(token)
                    .orElseThrow(() ->
                            new UnauthorizedException("Session expired"));

            LocalDateTime now = LocalDateTime.now();

            if (session.getLastActivityTime().plusMinutes(5).isBefore(now)) {

                customerSessionRepository.delete(session);
                throw new UnauthorizedException("Logged out due to inactivity");
            }

            session.setLastActivityTime(now);
            customerSessionRepository.save(session);

            authenticateUser(token, claims, request);
        } catch (Exception ex) {
            log.warn("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/customers/auth/login")
                || path.startsWith("/api/customers/register");
    }

    private void authenticateUser(String token,
                                  Claims claims,
                                  HttpServletRequest request) {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String email = claims.getSubject();
        if (email == null) return;

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.validateUserToken(token, userDetails)) {
            return;
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));

        CustomerPrincipal principal = new CustomerPrincipal(customer);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
