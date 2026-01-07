package com.ritik.customer_microservice.dto.transactionDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequestDTO {

    @NotNull
    private Long fromAccountNum;

    @NotNull
    private Long toAccountNum;

    @NotNull
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 4, max = 4, message = "PIN must be exactly 4 digits")
    private String pin;
}
