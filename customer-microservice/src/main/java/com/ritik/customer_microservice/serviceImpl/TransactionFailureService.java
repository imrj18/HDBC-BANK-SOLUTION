package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.exception.TransactionNotFoundException;
import com.ritik.customer_microservice.model.Transaction;
import com.ritik.customer_microservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionFailureService {

    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failTransactionDueToOtp(UUID transactionId, String email) {
        log.warn("Failing transaction due to OTP attempts exceeded | transactionId={} | email={}",transactionId,email);

        Transaction tx = transactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found while marking FAILED | transactionId={}", transactionId);
                    return new TransactionNotFoundException("Transaction not found");
                });
        tx.setTransactionStatus(TransactionStatus.FAILED);
        transactionRepository.save(tx);

        log.info(
                "Transaction marked as FAILED | transactionId={} | operation={} | amount={}",
                transactionId,
                tx.getOperationType(),
                tx.getAmount()
        );

        applicationEventPublisher.publishEvent(new TransactionEvent(
                transactionId,
                email,
                tx.getAmount(),
                tx.getOperationType(),
                tx.getTransactionType(),
                TransactionStatus.FAILED,
                "OTP ATTEMPT EXCEEDED"
        ));


        log.info("Failure transaction event published | transactionId={}", transactionId);
    }
}
