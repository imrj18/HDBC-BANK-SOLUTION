package com.ritik.customer_microservice.service;

import com.ritik.customer_microservice.dto.CustomerLoginDTO;
import com.ritik.customer_microservice.dto.CustomerRegisterDTO;
import com.ritik.customer_microservice.dto.CustomerResponseDTO;
import com.ritik.customer_microservice.dto.CustomerUpdateDTO;

public interface CustomerService {

    CustomerResponseDTO register(CustomerRegisterDTO registerDTO);

    String verify(CustomerLoginDTO loginDTO);

    CustomerResponseDTO viewProfile(String email);

    CustomerResponseDTO updateProfile(String email, CustomerUpdateDTO updateDTO);
}
