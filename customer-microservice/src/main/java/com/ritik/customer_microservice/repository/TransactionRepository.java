package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.dto.transactionDTO.TransactionHistoryDTO;
import com.ritik.customer_microservice.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByAccount_AccountId(UUID accountId, Pageable pageable);

    Page<Transaction> findByAccount_AccountIdIn(List<UUID> accountIds, Pageable pageable);
}
