package com.ritik.customer_microservice.dto.customerDTO;

import com.ritik.customer_microservice.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerResponseDTO {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String aadhar;

    private Status bankStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

