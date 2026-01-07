package com.ritik.customer_microservice.service;

import com.ritik.customer_microservice.dto.customerDTO.*;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerService {

    CustomerResponseDTO register(CustomerRegisterDTO registerDTO);

    String verify(CustomerLoginDTO loginDTO);

    CustomerResponseDTO viewProfile(String email);

    CustomerResponseDTO updateProfile(String email, CustomerUpdateDTO updateDTO);

    List<CustomerBalanceDTO> fetchCustomersByBankIdAndBalance(
            Long bankId,
            BigDecimal minBalance,
            BigDecimal maxBalance);
}