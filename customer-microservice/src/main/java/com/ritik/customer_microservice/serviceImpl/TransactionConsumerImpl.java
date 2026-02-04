package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import com.ritik.customer_microservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionConsumerImpl {

    private final EmailService emailService;

    private  final TransactionEmailTemplateService emailTemplateService;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "transaction-email-group"
    )

    public void transactionConsumer(TransactionEvent event, Acknowledgment acknowledgment){
        String subject = "Transaction Update";

        String body = emailTemplateService.buildEmailBody(event);

        emailService.sendMail(event.getEmail(),subject,body);
        acknowledgment.acknowledge();

    }
}
