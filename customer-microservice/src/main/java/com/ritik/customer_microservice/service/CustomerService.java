package com.ritik.customer_microservice.service;

import com.ritik.customer_microservice.dto.customerDTO.*;
import com.ritik.customer_microservice.serviceImpl.PageResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerService {

    CustomerResponseDTO register(CustomerRegisterDTO registerDTO);

    AuthResponseDTO verify(CustomerLoginDTO loginDTO);

    CustomerResponseDTO viewProfile(String email);

    CustomerResponseDTO updateProfile(String email, CustomerUpdateDTO updateDTO);

    PageResponse<CustomerBalanceDTO> fetchCustomersByBankIdAndBalance(
            Long bankId,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            int page,
            int size);
}