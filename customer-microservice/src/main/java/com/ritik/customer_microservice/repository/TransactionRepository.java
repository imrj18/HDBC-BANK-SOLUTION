package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.dto.transactionDTO.TransactionHistoryDTO;
import com.ritik.customer_microservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByAccount_AccountId(UUID accountId);

    List<Transaction> findByAccount_AccountIdIn(List<UUID> accountIds);
}
