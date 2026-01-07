package com.ritik.customer_microservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "customer_session")
@Getter
@Setter
public class CustomerSession {

    @Id
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(length = 500, nullable = false)
    private String token;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "last_activity_time", nullable = false)
    private LocalDateTime lastActivityTime;

}

