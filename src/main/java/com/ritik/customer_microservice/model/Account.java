package com.ritik.customer_microservice.model;


import com.ritik.customer_microservice.enums.AccountType;
import com.ritik.customer_microservice.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"account_num"})},
        indexes = {@Index(name = "idx_account_num", columnList = "account_num"),
                @Index(name = "idx_customer_id", columnList = "customer_id"),
                @Index(name = "idx_bank_id", columnList = "bank_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    @Column(name = "account_id", columnDefinition = "BINARY(16)")
    private UUID accountId;

    @Column(name = "customer_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID customerId;

    @Column(name = "account_num", nullable = false, unique = true)
    private Long accountNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "bank_id", nullable = false)
    private Long bankId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private Status accountStatus;

    @Column(name = "pin_hash", nullable = false, length = 255)
    private String pinHash;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
