package com.example.notification_microservice.consumer;

import com.example.notification_microservice.service.EmailService;
import com.hdbc.dto.event.OtpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "otp-events",
            groupId = "notification-group"
    )
    public void handleOtpEvent(
            OtpEvent event,
            Acknowledgment acknowledgment
    ) {

        try {
            emailService.sendMail(
                    event.getEmail(),
                    "OTP VERIFICATION",
                    "Your OTP is: " + event.getOtp() + " (valid for 5 minutes)"
            );

            acknowledgment.acknowledge();

            log.info("OTP email sent & offset acknowledged | email={}", event.getEmail());

        } catch (Exception ex) {
            log.error("Failed to process OTP event | email={}", event.getEmail(), ex);
            throw ex;
        }
    }

}
