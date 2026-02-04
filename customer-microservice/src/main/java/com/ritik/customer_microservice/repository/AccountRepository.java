package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.Account;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNum = :accountNum AND a.customer.customerId = :customerId")
    Optional<Account> lockAccount(Long accountNum, UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountId = :id")
    Account lockAccountById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNum = :accountNum")
    Account lockByAccountNum(@Param("accountNum") Long accountNum);

}
