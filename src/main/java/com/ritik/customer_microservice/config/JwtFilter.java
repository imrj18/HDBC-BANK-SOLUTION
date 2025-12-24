package com.ritik.customer_microservice.config;

import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.serviceImpl.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtServiceImpl jwtService;
    private final CustomerRepository customerRepository;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtServiceImpl jwtService, CustomerRepository customerRepository, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.customerRepository = customerRepository;
        this.userDetailsService=userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                Customer customer = customerRepository.findByEmail(email)
                            .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));

                CustomerPrincipal principal = new CustomerPrincipal(customer);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/customers/auth/login") || path.startsWith("/api/customers/register");
    }
}
