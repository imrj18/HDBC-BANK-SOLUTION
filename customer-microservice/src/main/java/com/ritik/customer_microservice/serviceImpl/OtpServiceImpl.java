package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.exception.BadRequestException;
import com.ritik.customer_microservice.model.OtpVerification;
import com.ritik.customer_microservice.repository.OtpRepository;
import com.ritik.customer_microservice.service.EmailService;
import com.ritik.customer_microservice.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    private final EmailService emailService;

    public String generateOtp(){
        return String.valueOf(new Random().nextInt(9000)+1000);
    }

    @Override
    public void sendOtp(String email, UUID transactionId) {
        String otp = generateOtp();

        OtpVerification verification = new OtpVerification();

        verification.setOtp(otp);
        verification.setTransactionId(transactionId);
        verification.setEmail(email);
        verification.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        verification.setVerified(false);
        verification.setAttemptCount((byte) 0);
        otpRepository.save(verification);
        emailService.sendMail(email, "OTP Verification", "Your OTP is: "+ otp + "(valid for 5 minutes)");
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, UUID transactionId, String otp) {

        OtpVerification verification = otpRepository.findByEmailAndTransactionIdAndVerifiedFalse(email, transactionId)
                        .orElseThrow(() -> new BadRequestException("OTP not found or already used"));

        if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.delete(verification);
            throw new BadRequestException("OTP expired. Transaction failed.");
        }

        if (verification.getAttemptCount() >= 3) {
            otpRepository.delete(verification);
            throw new BadRequestException("Too many invalid attempts. OTP invalidated.");
        }

        if (!verification.getOtp().equals(otp)) {
            verification.setAttemptCount((byte) (verification.getAttemptCount() + 1));
            otpRepository.save(verification);
            throw new BadRequestException("Invalid OTP");
        }

        verification.setVerified(true);
        otpRepository.save(verification);

        return true;
    }

}
