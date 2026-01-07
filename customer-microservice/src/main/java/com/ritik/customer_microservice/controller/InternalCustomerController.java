package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.customerDTO.CustomerBalanceDTO;
import com.ritik.customer_microservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/internal/customers")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final CustomerService service;

    @GetMapping
    @PreAuthorize("hasRole('SERVICE')")
    public List<CustomerBalanceDTO> getCustomers(@Valid
            @RequestParam Long bankId,
           @Valid @RequestParam(required = false) BigDecimal minBalance,
           @Valid @RequestParam(required = false) BigDecimal maxBalance) {

        return service.fetchCustomersByBankIdAndBalance(
                bankId, minBalance, maxBalance
        );
    }
}
