package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.OtpVerification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class OtpRepositoryTest {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private TestEntityManager entityManager;

    private OtpVerification otpVerification;
    private UUID transactionId;

    @BeforeEach
    void setUp(){
        transactionId = UUID.randomUUID();
        otpVerification = new OtpVerification();
        otpVerification.setOtp("0000");
        otpVerification.setAttemptCount((byte) 0);
        otpVerification.setEmail("test@example.com");
        otpVerification.setVerified(false);
        otpVerification.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpVerification.setTransactionId(transactionId);

        entityManager.persist(otpVerification);
        entityManager.flush();
    }

    @Test
    void shouldReturnOtpVerification_whenEmailAndTransactionIdMatch_andVerifiedIsFalse() {
        // Act
        Optional<OtpVerification> result =
                otpRepository.findByEmailAndTransactionIdAndVerifiedFalse(
                        "test@example.com",transactionId);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("0000", result.get().getOtp());
        Assertions.assertFalse(result.get().isVerified());
    }

    @Test
    void shouldReturnEmpty_whenOtpIsAlreadyVerified() {
        // Arrange
        otpVerification.setVerified(true);
        otpRepository.save(otpVerification);

        // Act
        Optional<OtpVerification> result = otpRepository.findByEmailAndTransactionIdAndVerifiedFalse(
                        "test@example.com", UUID.randomUUID());

        // Assert
        Assertions.assertTrue(result.isEmpty());
    }
}

