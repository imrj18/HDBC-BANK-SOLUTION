package com.ritik.bank_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBalanceDTO {

    private UUID customerId;

    private String name;

    private String email;

    private BigDecimal balance;
}
