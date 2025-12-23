package com.ritik.customer_microservice.service;

import com.ritik.customer_microservice.dto.CustomerLoginDTO;

public interface CustomerService {
    String verify(CustomerLoginDTO loginDTO);
}
