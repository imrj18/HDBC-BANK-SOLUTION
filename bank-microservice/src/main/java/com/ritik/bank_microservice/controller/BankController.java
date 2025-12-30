package com.ritik.bank_microservice.controller;

import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import com.ritik.bank_microservice.exception.BadRequestException;
import com.ritik.bank_microservice.exception.BankNotFoundException;
import com.ritik.bank_microservice.exception.CustomerNotFoundException;
import com.ritik.bank_microservice.feign.CustomerClient;
import com.ritik.bank_microservice.repository.BankRepository;
import com.ritik.bank_microservice.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banks")
public class BankController {

    private final BankService service;

    @PostMapping("/register")
    public ResponseEntity<BankResponseDTO> addBank(@Valid @RequestBody BankRequestDTO dto) {
        log.info("API call: POST /api/banks/register");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addBank(dto));
    }

    @GetMapping
    public ResponseEntity<List<BankResponseDTO>> getBanks(@RequestParam(required = false) String ifsc,
                                                          @RequestParam(required = false) Long bankId) {

        log.info("API call: GET /api/banks");
        return ResponseEntity.ok(service.getBankDetails(ifsc, bankId));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerBalanceDTO>> getCustomers(
            @RequestParam String ifsc,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance) {

        log.info("API call: GET /api/banks/customers");
        return ResponseEntity.ok(service.getCustomersByIfsc(ifsc, minBalance, maxBalance));
    }
}
