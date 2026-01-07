package com.ritik.customer_microservice.exception;

public class BankNotFoundException extends RuntimeException{
    public BankNotFoundException(String message){
        super(message);
    }
}
