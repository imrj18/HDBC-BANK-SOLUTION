package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;


    @Override
    public void sendMail(String to, String subject, String body) {

        log.info("Sending email | to={} | subject={}", to, subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Email sent successfully | to={} | subject={}", to, subject);

        } catch (Exception ex) {
            log.error("Failed to send email | to={} | subject={} | reason={}", to, subject, ex.getMessage(), ex);
            throw ex;
        }
    }

}
