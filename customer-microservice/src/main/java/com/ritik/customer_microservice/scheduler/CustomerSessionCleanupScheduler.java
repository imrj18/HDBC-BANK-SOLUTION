package com.ritik.customer_microservice.scheduler;

import com.ritik.customer_microservice.repository.CustomerSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomerSessionCleanupScheduler {

    private final CustomerSessionRepository customerSessionRepository;

    // 5 minutes
    @Scheduled(fixedRate = 120000)
    public void cleanupExpiredAndInactiveSessions() {

        LocalDateTime now = LocalDateTime.now();

        customerSessionRepository.deleteExpiredOrInactiveSessions(now, now.minusMinutes(4));
    }
}
