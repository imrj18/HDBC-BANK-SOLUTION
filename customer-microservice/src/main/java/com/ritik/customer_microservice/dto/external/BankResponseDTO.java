package com.ritik.customer_microservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankResponseDTO {

    private Long bankId;
    private String bankName;
    private String ifscCode;
    private String branch;
}

