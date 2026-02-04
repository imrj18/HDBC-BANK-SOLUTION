package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.model.OtpVerification;
import com.ritik.customer_microservice.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpAttemptService {

    private final OtpRepository otpRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttempt(OtpVerification verification) {

        log.debug(
                "Incrementing OTP attempt | otpId={} | currentAttempts={}",
                verification.getId(),
                verification.getAttemptCount()
        );

        verification.setAttemptCount(verification.getAttemptCount() + 1);
        otpRepository.save(verification);

        log.info("OTP attempt incremented | otpId={} | newAttempts={}", verification.getId(),
                verification.getAttemptCount());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteOtp(OtpVerification verification) {
        log.info("Deleting OTP verification | otpId={} | attempts={}", verification.getId(),
                verification.getAttemptCount());
        otpRepository.delete(verification);
        log.debug("OTP verification deleted successfully | otpId={}", verification.getId());
    }
}

