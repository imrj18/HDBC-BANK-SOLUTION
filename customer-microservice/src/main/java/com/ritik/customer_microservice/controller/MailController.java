package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMail(@RequestParam String email){
        emailService.sendMail(email,"Welcome to HDBC Bank", "Your account has been created successfully!");
        return ResponseEntity.ok("Mail sent successfully");
    }
}
