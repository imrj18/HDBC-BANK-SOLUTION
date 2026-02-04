package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.model.OtpVerification;
import com.ritik.customer_microservice.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpAttemptService {

    private final OtpRepository otpRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttempt(OtpVerification verification) {
        verification.setAttemptCount(verification.getAttemptCount() + 1);
        otpRepository.save(verification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteOtp(OtpVerification verification) {
        otpRepository.delete(verification);
    }
}

