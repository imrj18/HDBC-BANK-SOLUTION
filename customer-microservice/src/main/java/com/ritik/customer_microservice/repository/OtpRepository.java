package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

//    Optional<OtpVerification> findByEmailAndTransactionIdAndOtpAndVerifiedFalse(
//            String email,UUID transactionID, String otp
//    );

    @Modifying
    @Query("DELETE FROM OtpVerification  o where o.expiryTime < :now")
    void deleteExpiredOtps(@Param("now")LocalDateTime now);

    Optional<OtpVerification> findByEmailAndTransactionIdAndVerifiedFalse(String email, UUID transactionId);
}
