package com.ritik.customer_microservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
public class AccountBalanceDTO {

    private Long accountNumber;
    private BigDecimal accountBalance;
}
