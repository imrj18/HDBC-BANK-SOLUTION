package com.ritik.customer_microservice.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OtpAttemptsExceededException extends RuntimeException {

    private final UUID transactionId;
    private final String email;

    public OtpAttemptsExceededException(String message, UUID transactionId, String email) {
        super(message);
        this.transactionId = transactionId;
        this.email = email;
    }
}
