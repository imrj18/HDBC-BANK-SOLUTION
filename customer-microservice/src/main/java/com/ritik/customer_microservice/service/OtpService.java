package com.ritik.customer_microservice.service;

import java.util.UUID;

public interface OtpService {
    void sendOtp( String email, UUID transactionId);

    boolean verifyOtp(String email, UUID transactionId, String otp);
}
