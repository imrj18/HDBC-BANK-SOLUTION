package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.exception.TransactionNotFoundException;
import com.ritik.customer_microservice.model.Transaction;
import com.ritik.customer_microservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionFailureService {

    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failTransactionDueToOtp(UUID transactionId, String email) {

        Transaction tx = transactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        tx.setTransactionStatus(TransactionStatus.FAILED);
        transactionRepository.save(tx);

        applicationEventPublisher.publishEvent(new TransactionEvent(
                transactionId,
                email,
                tx.getAmount(),
                tx.getOperationType(),
                tx.getTransactionType(),
                TransactionStatus.FAILED,
                "OTP ATTEMPT EXCEEDED"
        ));
    }
}
