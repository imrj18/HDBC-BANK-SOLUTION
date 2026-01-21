package com.ritik.customer_microservice.dto.transactionDTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class ConfirmRequestDTO {

    private UUID transactionId;

    private String OTP;
}
