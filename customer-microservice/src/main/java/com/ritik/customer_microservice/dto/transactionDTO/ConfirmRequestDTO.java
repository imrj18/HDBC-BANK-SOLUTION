package com.ritik.customer_microservice.dto.transactionDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class ConfirmRequestDTO {

    @NotNull
    private UUID transactionId;

    @NotBlank
    @Size(min = 4, max = 4, message = "OTP must be 4 digits")
    private String OTP;
}
