package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByAadhar(String aadhar);

    boolean existsByEmail(String email);
}
