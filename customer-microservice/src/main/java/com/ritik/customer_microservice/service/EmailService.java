package com.ritik.customer_microservice.service;

public interface EmailService {
    void sendMail(String to, String subject, String body);
}
