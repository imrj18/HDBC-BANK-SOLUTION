package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.CustomerSession;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CustomerSessionRepository extends JpaRepository<CustomerSession, UUID> {
    Optional<CustomerSession> findByToken(String token);

    @Modifying
    @Transactional
    @Query(""" 
    DELETE FROM CustomerSession s
    WHERE s.expiryTime < :now
    OR s.lastActivityTime < :inactiveCutoff
    """)
    void deleteExpiredOrInactiveSessions(
            @Param("now") LocalDateTime now,
            @Param("inactiveCutoff") LocalDateTime inactiveCutoff
    );

}
