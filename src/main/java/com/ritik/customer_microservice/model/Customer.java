package com.ritik.customer_microservice.model;

import com.ritik.customer_microservice.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"aadhar"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @Column(name = "customer_id", columnDefinition = "BINARY(16)")
    private UUID customerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false, length = 15, unique = true)
    private String phone;

    @Column(nullable = false, length = 250)
    private String address;

    @Column(nullable = false, length = 12, unique = true)
    private String aadhar;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status", nullable = false)
    private Status status;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<Account> accounts = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.customerId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
