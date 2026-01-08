package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountNumAndCustomer_CustomerId(Long accountNum, UUID customerId);

    List<Account> findByCustomer_CustomerId(UUID customerId);

    Optional<Account> findByAccountNum(@NotNull Long fromAccountNum);

    //List<Account> findByCustomer_(Customer customer);
}
