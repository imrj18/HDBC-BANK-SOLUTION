package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import com.ritik.customer_microservice.enums.OperationType;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.enums.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TransactionEmailTemplateServiceTest {

    @InjectMocks
    private TransactionEmailTemplateService service;

    @Test
    void shouldBuildWithdrawSuccessMessage(){
        //Arrange
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID(),
                "user@example.com",
                BigDecimal.valueOf(1000),
                OperationType.WITHDRAW,
                TransactionType.DEBIT,
                TransactionStatus.SUCCESS,
                null
        );

        //Act
        String result = service.buildEmailBody(event);

        //Assert
        Assertions.assertTrue(result.contains("1000"));
        Assertions.assertTrue(result.contains("debited successfully"));
        Assertions.assertTrue(result.contains(event.getTransactionId().toString()));
    }

    @Test
    void shouldBuildWithdrawFailureMessage() {
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID(),
                "user@test.com",
                BigDecimal.valueOf(1000),
                OperationType.WITHDRAW,
                TransactionType.DEBIT,
                TransactionStatus.FAILED,
                "OTP FAILED"
        );

        String result = service.buildEmailBody(event);

        Assertions.assertTrue(result.contains("Withdrawal failed"));
        Assertions.assertTrue(result.contains(event.getTransactionId().toString()));
    }

    @Test
    void shouldBuildDepositSuccessMessage(){
        //Arrange
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID(),
                "user@example.com",
                BigDecimal.valueOf(1000),
                OperationType.DEPOSIT,
                TransactionType.CREDIT,
                TransactionStatus.SUCCESS,
                null
        );

        //Act
        String result = service.buildEmailBody(event);

        //Assert
        Assertions.assertTrue(result.contains("1000"));
        Assertions.assertTrue(result.contains("credited successfully"));
        Assertions.assertTrue(result.contains(event.getTransactionId().toString()));
    }

    @Test
    void shouldBuildTransferSuccessMessage() {
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID(),
                "user@test.com",
                BigDecimal.valueOf(1500),
                OperationType.TRANSFER,
                TransactionType.DEBIT,
                TransactionStatus.SUCCESS,
                null
        );

        String result = service.buildEmailBody(event);

        Assertions.assertTrue(result.contains("1500"));
        Assertions.assertTrue(result.contains("transferred successfully"));
        Assertions.assertTrue(result.contains(event.getTransactionId().toString()));
    }

    @Test
    void shouldBuildTransferFailureMessage() {
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID(),
                "user@test.com",
                BigDecimal.valueOf(1500),
                OperationType.TRANSFER,
                TransactionType.DEBIT,
                TransactionStatus.FAILED,
                "OTP FAILED"
        );

        String result = service.buildEmailBody(event);

        Assertions.assertTrue(result.contains("Transfer failed"));
        Assertions.assertTrue(result.contains(event.getTransactionId().toString()));
    }

}
