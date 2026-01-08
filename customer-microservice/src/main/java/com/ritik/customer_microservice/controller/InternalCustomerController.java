package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.customerDTO.CustomerBalanceDTO;
import com.ritik.customer_microservice.service.CustomerService;
import com.ritik.customer_microservice.serviceImpl.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public PageResponse<CustomerBalanceDTO> getCustomers(@Valid @RequestParam Long bankId,
                                                         @Valid @RequestParam(required = false) BigDecimal minBalance,
                                                         @Valid @RequestParam(required = false) BigDecimal maxBalance,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "5") int size) {

        return service.fetchCustomersByBankIdAndBalance(bankId, minBalance, maxBalance, page, size);
    }
}
