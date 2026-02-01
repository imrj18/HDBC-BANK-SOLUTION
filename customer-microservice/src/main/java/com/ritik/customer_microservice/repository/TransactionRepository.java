package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Transaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByAccount_AccountId(UUID accountId, Pageable pageable);

    Page<Transaction> findByAccount_AccountIdIn(List<UUID> accountIds, Pageable pageable);

    Optional<Transaction> findByTransactionId(UUID transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.account.accountId = :id")
    Optional<Transaction> lockByTransactionId(@Param("id") UUID id);
}
