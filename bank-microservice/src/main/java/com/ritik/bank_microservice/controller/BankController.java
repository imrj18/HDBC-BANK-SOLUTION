package com.ritik.bank_microservice.controller;

import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import com.ritik.bank_microservice.service.BankService;
import com.ritik.bank_microservice.wrapper.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banks")
@Validated
public class BankController {

    private final BankService service;

    @PostMapping("/register")
    public ResponseEntity<BankResponseDTO> addBank(@Valid @RequestBody BankRequestDTO dto) {
        log.info("API call: POST /api/banks/register");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addBank(dto));
    }

    @GetMapping
    public ResponseEntity<PageResponse<BankResponseDTO>> getBanks(@RequestParam(required = false)
                                                                      @Pattern(
                                                                              regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                                                                              message = "Invalid IFSC format"
                                                                      )
                                                                      String ifsc,
                                                                  @RequestParam(required = false) @Positive Long bankId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "5") int size) {

        log.info("API call: GET /api/banks");
        return ResponseEntity.ok(service.getBankDetails(ifsc, bankId, page, size));
    }

    @GetMapping("/customers")
    public ResponseEntity<PageResponse<CustomerBalanceDTO>> getCustomers(
            @RequestParam
            @NotBlank(message = "IFSC is required")
            @Pattern(
                    regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                    message = "Invalid IFSC format"
            )
            String ifsc,
             @RequestParam(required = false) BigDecimal minBalance,
             @RequestParam(required = false) BigDecimal maxBalance,
             @RequestParam(defaultValue = "0", required = false) int page,
             @RequestParam(defaultValue = "5", required = false) int size) {

        log.info("API call: GET /api/banks/customers");
        return ResponseEntity.ok(service.getCustomersByIfsc(ifsc, minBalance, maxBalance, page, size));
    }
}
