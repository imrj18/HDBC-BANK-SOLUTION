package com.example.notification_microservice.consumer;

import com.example.notification_microservice.enums.TransactionStatus;
import com.example.notification_microservice.service.EmailService;
import com.example.notification_microservice.service.TransactionEmailTemplateService;

import com.hdbc.dto.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConsumer {

    private final EmailService emailService;
    private final TransactionEmailTemplateService emailTemplateService;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "notification-group"
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

            String subject;
            String body;

            if (event.getStatus().equals(TransactionStatus.FAILED.toString())) {

                subject = "Transaction Failed";
                body = emailTemplateService.buildFailureEmail(event);

            } else {

                subject = "Transaction Update";
                body = emailTemplateService.buildEmailBody(event);
            }

            emailService.sendMail(event.getEmail(), subject, body);

            acknowledgment.acknowledge();

            log.info(
                    "Transaction email sent & offset acknowledged | transactionId={}",
                    event.getTransactionId()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to process transaction event | transactionId={} | reason={}",
                    event.getTransactionId(),
                    ex.getMessage(),
                    ex
            );

            throw ex; // Kafka retry
        }
    }

}
