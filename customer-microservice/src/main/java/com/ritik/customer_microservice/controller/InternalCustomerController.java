package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.customerDTO.CustomerBalanceDTO;
import com.ritik.customer_microservice.service.CustomerService;
import com.ritik.customer_microservice.wrapper.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/internal/customers")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final CustomerService service;

    @GetMapping
    @PreAuthorize("hasRole('SERVICE')")
    public PageResponse<CustomerBalanceDTO> getCustomers(
            @Valid @RequestParam Long bankId,
            @Valid @RequestParam(required = false) BigDecimal minBalance,
            @Valid @RequestParam(required = false) BigDecimal maxBalance,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("INTERNAL API call: FETCH CUSTOMERS | bankId={} | minBalance={} | maxBalance={} | page={} | size={}",
                bankId, minBalance, maxBalance, page, size);

        PageResponse<CustomerBalanceDTO> response = service.fetchCustomersByBankIdAndBalance(
                bankId, minBalance, maxBalance, page, size);

        log.info("INTERNAL API response: FETCH CUSTOMERS | bankId={} | resultCount={}",
                bankId, response.getTotalItems());

        return response;
    }
}
