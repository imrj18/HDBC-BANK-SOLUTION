package com.ritik.customer_microservice.dto.customerDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
}
