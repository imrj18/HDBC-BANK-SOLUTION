package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerUserDetailsService implements UserDetailsService {
    private final CustomerRepository repository;

    public CustomerUserDetailsService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user for authentication | email={}", email);
        Customer customer = repository.findByEmail(email).orElseThrow(() -> {
            log.warn("Authentication failed | reason=USER_NOT_FOUND | email={}", email);
            return new UsernameNotFoundException("Customer not found");
        });
        return new CustomerPrincipal(customer);
    }
}
