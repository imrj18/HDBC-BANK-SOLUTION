package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.repository.CustomerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService {
    private final CustomerRepository repository;

    public CustomerUserDetailsService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = repository.findByEmail(email).orElseThrow(() ->
                        new UsernameNotFoundException("Customer not found"));
        return new CustomerPrincipal(customer);
    }
}
