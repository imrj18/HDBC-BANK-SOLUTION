package com.ritik.customer_microservice.service;


import com.hdbc.dto.event.OtpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class OtpProducer {

    private final KafkaTemplate<String, OtpEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OtpEvent event) {
        log.info(
                "Publishing OTP event to Kafka | email={} | otp={}",
                event.getEmail(),
                event.getOtp()
        );

        Message<OtpEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "otp-events")
                .setHeader(KafkaHeaders.KEY, event.getEmail())
                .build();

        kafkaTemplate.send(message);
    }
}

