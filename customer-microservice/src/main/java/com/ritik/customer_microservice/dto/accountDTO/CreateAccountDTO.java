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

    @NotBlank(message = "IFSC code is required")
    @Size(min = 11, max = 11, message = "IFSC must be 11 characters")
    private String ifscCode;

    @NotNull(message = "PIN is required")
    @Pattern(regexp = "^-?\\d+$", message = "Must be valid number.")
    @Size(min = 4, max = 4, message = "PIN must be 4 to 6 digits")
    private String pin;
}
