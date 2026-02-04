package com.ritik.customer_microservice.scheduler;

import com.ritik.customer_microservice.repository.CustomerSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerSessionCleanupScheduler {

    private final CustomerSessionRepository customerSessionRepository;

    // Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredAndInactiveSessions() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inactiveBefore = now.minusMinutes(3);

        log.info("Customer session cleanup started | now={} | inactiveBefore={}", now, inactiveBefore);

        long startTime = System.currentTimeMillis();

        try {
            int deletedCount = customerSessionRepository.deleteExpiredOrInactiveSessions(now, inactiveBefore);

            long duration = System.currentTimeMillis() - startTime;

            log.info("Customer session cleanup completed | deletedSessions={} | duration={} ms", deletedCount,duration);

        } catch (Exception ex) {
            log.error("Customer session cleanup failed | now={} | inactiveBefore={}", now, inactiveBefore, ex);
        }
    }
}
