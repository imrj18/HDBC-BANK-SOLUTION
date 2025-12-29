package com.ritik.customer_microservice.service;

import com.ritik.customer_microservice.dto.customerDTO.CustomerLoginDTO;
import com.ritik.customer_microservice.dto.customerDTO.CustomerRegisterDTO;
import com.ritik.customer_microservice.dto.customerDTO.CustomerResponseDTO;
import com.ritik.customer_microservice.dto.customerDTO.CustomerUpdateDTO;

public interface CustomerService {

    CustomerResponseDTO register(CustomerRegisterDTO registerDTO);

    String verify(CustomerLoginDTO loginDTO);

    CustomerResponseDTO viewProfile(String email);

    CustomerResponseDTO updateProfile(String email, CustomerUpdateDTO updateDTO);
}
