package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.CustomerLoginDTO;
import com.ritik.customer_microservice.dto.CustomerRegisterDTO;
import com.ritik.customer_microservice.dto.CustomerResponseDTO;
import com.ritik.customer_microservice.dto.CustomerUpdateDTO;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.serviceImpl.CustomerServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerServiceImpl service;

    public CustomerController(CustomerServiceImpl service) {
        this.service = service;
    }

    @GetMapping
    public String greet(){

        return "Hello World";
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@Valid @RequestBody CustomerLoginDTO loginDTO){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.verify(loginDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDTO> register(@Valid @RequestBody CustomerRegisterDTO registerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(registerDTO));
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> profile(@AuthenticationPrincipal CustomerPrincipal principal){
        String email = principal.getUsername();
        return ResponseEntity.status(HttpStatus.OK).body(service.viewProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> updateProfile(@AuthenticationPrincipal CustomerPrincipal principal,
                                                            @Valid @RequestBody CustomerUpdateDTO customerUpdateDTO){
        String email = principal.getUsername();
        return ResponseEntity.status(HttpStatus.OK).body(service.updateProfile(email, customerUpdateDTO));
    }
}
