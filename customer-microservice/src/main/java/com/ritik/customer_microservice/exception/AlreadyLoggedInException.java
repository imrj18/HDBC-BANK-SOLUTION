package com.ritik.customer_microservice.exception;

public class AlreadyLoggedInException extends RuntimeException {

    public AlreadyLoggedInException(String message) {
        super(message);
    }
}

