package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import com.ritik.customer_microservice.enums.OperationType;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.enums.TransactionType;
import com.ritik.customer_microservice.exception.TransactionNotFoundException;
import com.ritik.customer_microservice.model.Transaction;
import com.ritik.customer_microservice.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TransactionFailureServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TransactionFailureService transactionFailureService;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    @Test
    void shouldFailTransactionAndPublishEvent() {
        // Arrange
        UUID txId = UUID.randomUUID();
        String email = "user@test.com";

        Transaction tx = new Transaction();
        tx.setTransactionId(txId);
        tx.setAmount(BigDecimal.valueOf(1000));
        tx.setOperationType(OperationType.TRANSFER);
        tx.setTransactionType(TransactionType.DEBIT);
        tx.setTransactionStatus(TransactionStatus.PENDING);

        Mockito.when(transactionRepository.findByTransactionId(txId)).thenReturn(Optional.of(tx));

        // Act
        transactionFailureService.failTransactionDueToOtp(txId, email);

        // Assert
        Assertions.assertEquals(TransactionStatus.FAILED, tx.getTransactionStatus());

        Mockito.verify(transactionRepository).save(tx);

        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        Object publishedEvent = eventCaptor.getValue();

        Assertions.assertTrue(publishedEvent instanceof TransactionEvent);

        TransactionEvent event = (TransactionEvent) publishedEvent;

        Assertions.assertEquals(txId, event.getTransactionId());
        Assertions.assertEquals(TransactionStatus.FAILED, event.getStatus());
        Assertions.assertEquals("OTP ATTEMPT EXCEEDED", event.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Arrange
        UUID txId = UUID.randomUUID();

        Mockito.when(transactionRepository.findByTransactionId(txId)).thenReturn(Optional.empty());

        // Act + Assert
        Assertions.assertThrows(
                TransactionNotFoundException.class,
                () -> transactionFailureService.failTransactionDueToOtp(txId, "user@test.com")
        );

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any());
    }


}