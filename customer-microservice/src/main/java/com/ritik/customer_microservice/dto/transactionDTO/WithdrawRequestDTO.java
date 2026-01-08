package com.ritik.customer_microservice.dto.transactionDTO;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WithdrawRequestDTO {

    @NotNull
    @Positive(message = "Account number must be positive")
    private Long accountNum;

    @NotNull
    @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 4, max = 4, message = "PIN must be 4 digits")
    private String pin;
}
