package com.ritik.bank_microservice.exception;

public class IfscCodeAlreadyExistException  extends RuntimeException {
    public IfscCodeAlreadyExistException(String message) {
        super(message);
    }
}
