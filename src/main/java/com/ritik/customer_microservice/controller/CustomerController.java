package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.CustomerLoginDTO;
import com.ritik.customer_microservice.dto.CustomerRegisterDTO;
import com.ritik.customer_microservice.dto.CustomerResponseDTO;
import com.ritik.customer_microservice.serviceImpl.CustomerServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerServiceImpl service;

    @GetMapping
    public String greet(){

        return "Hello World";
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@Valid @RequestBody CustomerLoginDTO loginDTO){
        log.info("LOGIN CONTROLLER HIT");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.verify(loginDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDTO> register(@Valid @RequestBody CustomerRegisterDTO registerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(registerDTO));
    }
}
