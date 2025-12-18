package com.ritik.bank_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BankResponseDTO {

    private Long bankId;
    private String bankName;
    private String ifscCode;
    private String branch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}