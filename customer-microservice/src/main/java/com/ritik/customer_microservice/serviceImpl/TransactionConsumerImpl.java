package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import com.ritik.customer_microservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConsumerImpl {

    private final EmailService emailService;
    private final TransactionEmailTemplateService emailTemplateService;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "transaction-email-group"
    )
    public void transactionConsumer(
            TransactionEvent event,
            Acknowledgment acknowledgment
    ) {

        log.info(
                "Transaction event received | transactionId={} | operation={} | status={} | email={}",
                event.getTransactionId(),
                event.getOperationType(),
                event.getStatus(),
                event.getEmail()
        );

        try {
            String subject = "Transaction Update";

            String body = emailTemplateService.buildEmailBody(event);

            emailService.sendMail(event.getEmail(), subject, body);
            acknowledgment.acknowledge();

            log.info("Transaction email sent & offset acknowledged | transactionId={}", event.getTransactionId());

        } catch (Exception ex) {

            log.error(
                    "Failed to process transaction event | transactionId={} | reason={}",
                    event.getTransactionId(),
                    ex.getMessage(),
                    ex
            );

            throw ex;
        }
    }
}

