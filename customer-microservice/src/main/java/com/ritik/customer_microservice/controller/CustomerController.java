package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.customerDTO.*;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody CustomerLoginDTO loginDTO) {

        log.info("API call: CUSTOMER LOGIN for email={}", loginDTO.getEmail());

        return ResponseEntity.ok(service.verify(loginDTO));
    }


    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDTO> register(@Valid @RequestBody CustomerRegisterDTO registerDTO) {

        log.info("API call: CUSTOMER REGISTER");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(registerDTO));
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> profile(@AuthenticationPrincipal CustomerPrincipal principal) {

        String email = principal.getUsername();
        log.info("API call: VIEW PROFILE | user={}", email);

        return ResponseEntity.ok(service.viewProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> updateProfile(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody CustomerUpdateDTO customerUpdateDTO) {

        String email = principal.getUsername();
        log.info("API call: UPDATE PROFILE | user={}", email);

        return ResponseEntity.ok(service.updateProfile(email, customerUpdateDTO));
    }
}

