package com.ritik.customer_microservice.service;


import com.hdbc.dto.event.OtpEvent;
import com.ritik.customer_microservice.exception.BadRequestException;
import com.ritik.customer_microservice.exception.OtpAttemptsExceededException;
import com.ritik.customer_microservice.model.OtpVerification;
import com.ritik.customer_microservice.repository.OtpRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {

    private final OtpRepository otpRepository;
    //private final EmailService emailService;
    private final OtpAttemptService otpAttemptService;
    private final TransactionFailureService transactionFailureService;
    private final OtpProducer otpProducer;

    public String generateOtp() {
        log.debug("Generating OTP");
        return String.valueOf(new Random().nextInt(9000) + 1000);
    }

    public void sendOtp(String email, UUID transactionId) {

        log.info("Sending OTP | email={} | transactionId={}", email, transactionId);
        String otp = generateOtp();

        OtpVerification verification = new OtpVerification();
        verification.setOtp(otp);
        verification.setTransactionId(transactionId);
        verification.setEmail(email);
        verification.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        verification.setVerified(false);
        verification.setAttemptCount(0);

        otpRepository.save(verification);

        OtpEvent event = new OtpEvent(email, otp, "Debit Transaction");
        otpProducer.publish(event);

        log.debug("OTP record created | otpId={} | expiryTime={}", verification.getId(), verification.getExpiryTime());

//        emailService.sendMail(email, "OTP Verification", "Your OTP is: " + otp + " (valid for 5 minutes)");
//
//        log.info("OTP email sent successfully | email={} | transactionId={}", email, transactionId);
    }

    @Transactional
    public boolean verifyOtp(String email, UUID transactionId, String otp) {

        log.info(
                "Verifying OTP | email={} | transactionId={}",
                email,
                transactionId
        );

        OtpVerification verification =
                otpRepository.findByEmailAndTransactionIdAndVerifiedFalse(email, transactionId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "OTP verification failed - not found or already used | email={} | transactionId={}",
                                    email,
                                    transactionId
                            );
                            return new BadRequestException("OTP not found or already used");
                        });

        if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {

            log.warn("OTP expired | otpId={} | transactionId={}", verification.getId(), transactionId);
            otpAttemptService.deleteOtp(verification);
            throw new BadRequestException("OTP expired. Transaction failed.");
        }

        if (verification.getAttemptCount() >= 3) {

            log.error(
                    "OTP attempts exceeded | otpId={} | transactionId={} | attempts={}",
                    verification.getId(),
                    transactionId,
                    verification.getAttemptCount()
            );

            otpAttemptService.deleteOtp(verification);
            throw new OtpAttemptsExceededException("Too many invalid attempts. OTP invalidated.", transactionId,email);
        }

        if (!verification.getOtp().equals(otp)) {

            log.warn(
                    "Invalid OTP attempt | otpId={} | transactionId={} | attempts={}",
                    verification.getId(),
                    transactionId,
                    verification.getAttemptCount() + 1
            );

            otpAttemptService.incrementAttempt(verification);
            throw new BadRequestException("Invalid OTP");
        }

        verification.setVerified(true);
        otpRepository.save(verification);

        log.info("OTP verified successfully | otpId={} | transactionId={}", verification.getId(), transactionId);
        return true;
    }
}
