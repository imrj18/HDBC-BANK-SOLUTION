package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByAadhar(String aadhar);

    boolean existsByEmail(String email);

    @Query("""
    SELECT c, a.amount
    FROM Customer c
    JOIN c.accounts a
    WHERE a.bankId = :bankId
      AND (:minBalance IS NULL OR a.amount >= :minBalance)
      AND (:maxBalance IS NULL OR a.amount <= :maxBalance)
""")
    List<Object[]> findCustomersByBankIdAndBalance(
            @Param("bankId") Long bankId,
            @Param("minBalance") BigDecimal minBalance,
            @Param("maxBalance") BigDecimal maxBalance
    );
}

