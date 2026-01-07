package com.ritik.customer_microservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_number_generator")
@Getter
@NoArgsConstructor
public class AccountNumberGenerator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
