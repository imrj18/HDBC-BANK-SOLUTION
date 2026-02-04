package com.ritik.customer_microservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
public class KafkaTopicConfig {
    private static final String TRANSACTION_TOPIC = "transaction-events";

    @Bean
    public NewTopic transactionTopic(){
        log.info(
                "Configuring Kafka topic | name={} | partitions={} | replicas={}",
                TRANSACTION_TOPIC,
                3,
                1
        );

        return TopicBuilder
                .name(TRANSACTION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
