package com.ritik.bank_microservice.exception;

public class InvalidIfscCodeException extends RuntimeException{
    public InvalidIfscCodeException(String message){
        super(message);
    }
}
