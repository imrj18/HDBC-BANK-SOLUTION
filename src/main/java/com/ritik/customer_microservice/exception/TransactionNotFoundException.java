package com.ritik.customer_microservice.exception;

public class TransactionNotFoundException extends RuntimeException{
    public TransactionNotFoundException(String message){
        super(message);
    }
}
