package com.ritik.customer_microservice.scheduler;

import com.ritik.customer_microservice.repository.CustomerSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class CustomerSessionCleanupSchedulerTest {

    @Mock
    private CustomerSessionRepository customerSessionRepository;

    @InjectMocks
    private CustomerSessionCleanupScheduler scheduler;

    @Test
    void shouldTriggerSessionCleanup() {
        scheduler.cleanupExpiredAndInactiveSessions();

        Mockito.verify(customerSessionRepository, Mockito.times(1))
                .deleteExpiredOrInactiveSessions(
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)
                );
    }
}
