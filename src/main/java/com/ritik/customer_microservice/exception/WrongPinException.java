package com.ritik.customer_microservice.exception;

public class WrongPinException extends RuntimeException {
    public WrongPinException(String message) {
        super(message);
    }
}
