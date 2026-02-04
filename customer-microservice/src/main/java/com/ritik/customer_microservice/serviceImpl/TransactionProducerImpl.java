package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionProducerImpl {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(TransactionEvent event) {
        log.info(
                "Publishing transaction event to Kafka | transactionId={} | operation={} | status={}",
                event.getTransactionId(),
                event.getOperationType(),
                event.getStatus()
        );

        kafkaTemplate.send(
                "transaction-events",
                event.getTransactionId().toString(),
                event
        );
    }
}

