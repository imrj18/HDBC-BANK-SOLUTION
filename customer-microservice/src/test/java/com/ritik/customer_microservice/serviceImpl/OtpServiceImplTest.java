package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.exception.BadRequestException;
import com.ritik.customer_microservice.exception.OtpAttemptsExceededException;
import com.ritik.customer_microservice.model.OtpVerification;
import com.ritik.customer_microservice.repository.OtpRepository;
import com.ritik.customer_microservice.service.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpAttemptService otpAttemptService;


    @InjectMocks
    private OtpServiceImpl otpService;

    private UUID transactionId;
    private String email;
    private OtpVerification verification;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        email = "test@gmail.com";

        verification = new OtpVerification();
        verification.setTransactionId(transactionId);
        verification.setEmail(email);
        verification.setOtp("1234");
        verification.setVerified(false);
        verification.setAttemptCount(0);
        verification.setExpiryTime(LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void shouldSendOtpSuccessfully() {
        // Arrange
        Mockito.when(otpRepository.save(Mockito.any(OtpVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act + Assert
        otpService.sendOtp(email, transactionId);

        Mockito.verify(otpRepository).save(Mockito.any(OtpVerification.class));
        Mockito.verify(emailService).sendMail(
                Mockito.eq(email),
                Mockito.eq("OTP Verification"),
                Mockito.contains("Your OTP is")
        );
    }

    @Test
    void shouldVerifyOtpSuccessfully() {
        // Arrange
        Mockito.when(otpRepository.findByEmailAndTransactionIdAndVerifiedFalse(email, transactionId))
                .thenReturn(Optional.of(verification));

        Mockito.when(otpRepository.save(Mockito.any(OtpVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean result = otpService.verifyOtp(email, transactionId, "1234");

        // Assert
        Assertions.assertTrue(result);
        Assertions.assertTrue(verification.isVerified());

        Mockito.verify(otpRepository).save(verification);
    }

    @Test
    void shouldThrowExceptionWhenOtpNotFound() {
        // Arrange
        Mockito.when(otpRepository.findByEmailAndTransactionIdAndVerifiedFalse(email, transactionId))
                .thenReturn(Optional.empty());

        // Act + Assert
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class,
                () -> otpService.verifyOtp(email, transactionId, "1234"));

        Assertions.assertEquals("OTP not found or already used", ex.getMessage());

        Mockito.verify(otpRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenOtpExpired() {
        // Arrange
        verification.setExpiryTime(LocalDateTime.now().minusMinutes(1));

        Mockito.when(otpRepository
                        .findByEmailAndTransactionIdAndVerifiedFalse(email, transactionId))
                .thenReturn(Optional.of(verification));

        // Act + Assert
        BadRequestException ex = Assertions.assertThrows(
                BadRequestException.class,
                () -> otpService.verifyOtp(email, transactionId, "1234")
        );

        Assertions.assertEquals("OTP expired. Transaction failed.", ex.getMessage());

        Mockito.verify(otpAttemptService).deleteOtp(verification);
    }

    @Test
    void shouldThrowExceptionWhenTooManyAttempts() {
        // Arrange
        verification.setAttemptCount(3);

        Mockito.when(otpRepository.findByEmailAndTransactionIdAndVerifiedFalse(email, transactionId))
                .thenReturn(Optional.of(verification));

        // Act + Assert
        OtpAttemptsExceededException ex = Assertions.assertThrows(OtpAttemptsExceededException.class,
                () -> otpService.verifyOtp(email, transactionId, "1234"));

        Assertions.assertEquals("Too many invalid attempts. OTP invalidated.", ex.getMessage());

        Mockito.verify(otpAttemptService).deleteOtp(verification);
    }

    @Test
    void shouldIncrementAttemptCountWhenOtpInvalid() {
        // Arrange
        verification.setAttemptCount(1);
        Mockito.when(otpRepository
                        .findByEmailAndTransactionIdAndVerifiedFalse(email, transactionId))
                .thenReturn(Optional.of(verification));

        // Act + Assert
        BadRequestException ex = Assertions.assertThrows(
                BadRequestException.class, () -> otpService.verifyOtp(email, transactionId, "9999"));

        Assertions.assertEquals("Invalid OTP", ex.getMessage());
        Assertions.assertEquals(1, verification.getAttemptCount());

        Mockito.verify(otpAttemptService).incrementAttempt(verification);
    }


}

