package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.CustomerSession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerSessionRepository extends JpaRepository<CustomerSession, UUID> {
    Optional<CustomerSession> findByToken(String token);

    boolean existsByToken(String token);
}
