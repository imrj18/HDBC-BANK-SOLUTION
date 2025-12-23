package com.ritik.customer_microservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerResponseDTO {
    private String customerId;
    private Long bankId;

    private String name;
    private String email;
    private String phone;
    private String aadhar;

    private String bankStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

