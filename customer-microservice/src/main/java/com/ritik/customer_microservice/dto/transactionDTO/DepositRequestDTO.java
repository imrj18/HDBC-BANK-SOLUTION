package com.ritik.customer_microservice.dto.transactionDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequestDTO {

    @NotNull
    @Positive(message = "Account number must be positive")
    private Long accountNum;

    @NotNull
    @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
    private BigDecimal amount;
}
