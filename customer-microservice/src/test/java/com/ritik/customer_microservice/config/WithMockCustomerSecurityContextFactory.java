package com.ritik.customer_microservice.config;

import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomerSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomer> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomer annotation) {

        Customer customer = new Customer();
        customer.setEmail(annotation.email());

        CustomerPrincipal principal = new CustomerPrincipal(customer);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }
}
