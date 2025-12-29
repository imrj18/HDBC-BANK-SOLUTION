package com.ritik.customer_microservice.dto.accountDTO;

import com.ritik.customer_microservice.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountDTO {
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Bank ID is required")
    private Long bankId;

    @NotNull(message = "PIN is required")
    @Size(min = 4, max = 4, message = "PIN must be 4 to 6 digits")
    private String pin;
}
