package com.ritik.customer_microservice.serviceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void shouldSendEmailSuccessfully() {
        // Arrange
        String to = "test@gmail.com";
        String subject = "Test Subject";
        String body = "Test email body";

        // Act
        emailService.sendMail(to, subject, body);

        // Assert
        Mockito.verify(mailSender).send(Mockito.any(SimpleMailMessage.class));
    }

}
