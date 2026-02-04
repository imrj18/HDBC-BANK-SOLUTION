package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.model.OtpVerification;
import com.ritik.customer_microservice.repository.OtpRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtpAttemptServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @InjectMocks
    private OtpAttemptService service;

    private OtpVerification otpVerification;

    @BeforeEach
    void setup(){
        otpVerification = new OtpVerification();
        otpVerification.setAttemptCount(0);
    }

    @Test
    void shouldIncrementAttemptSuccessfully(){
        // Act
        service.incrementAttempt(otpVerification);

        // Assert
        Assertions.assertEquals(1, otpVerification.getAttemptCount());
        Mockito.verify(otpRepository).save(otpVerification);
    }

    @Test
    void shouldDeleteOtpSuccessfully(){
        //Act
        service.deleteOtp(otpVerification);

        //Assert
        Mockito.verify(otpRepository,Mockito.times(1)).delete(otpVerification);
    }
}
