package com.ritik.customer_microservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="otp_verification",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"transaction_id"})
        }
)
@Getter
@Setter
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "transaction_id",nullable = false)
    private UUID transactionId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "otp",nullable = false)
    private String otp;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name="attempt_count",nullable = false)
    private int attemptCount;

    @Column(name = "verified", nullable = false)
    private boolean verified;
}
