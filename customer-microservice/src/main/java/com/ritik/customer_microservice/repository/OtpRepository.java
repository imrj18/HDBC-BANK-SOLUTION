package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

//    @Modifying
//    @Query("DELETE FROM OtpVerification  o where o.expiryTime < :now")
//    void deleteExpiredOtps(@Param("now")LocalDateTime now);

    Optional<OtpVerification> findByEmailAndTransactionIdAndVerifiedFalse(String email, UUID transactionId);
}
