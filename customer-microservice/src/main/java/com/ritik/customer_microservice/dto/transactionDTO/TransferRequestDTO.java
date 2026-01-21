package com.ritik.customer_microservice.dto.transactionDTO;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
public class TransferRequestDTO {

    @NotNull
    @Positive(message = "Account number must be positive")
    private Long fromAccountNum;

    @NotNull
    @Positive(message = "Account number must be positive")
    private Long toAccountNum;

    @NotNull
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 4, max = 4, message = "PIN must be exactly 4 digits")
    private String pin;

    public TransferRequestDTO(Long counterpartyAccountNum, Long accountNum, BigDecimal amount) {
        this.toAccountNum = counterpartyAccountNum;
        this.fromAccountNum = accountNum;
        this.amount = amount;
    }
}
