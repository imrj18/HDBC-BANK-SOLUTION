package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.dto.AccountResponseDTO;
import com.ritik.customer_microservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountNumAndCustomerId(Long accountNum, UUID customerId);

    List<Account> findByCustomerId(UUID customerId);
}
