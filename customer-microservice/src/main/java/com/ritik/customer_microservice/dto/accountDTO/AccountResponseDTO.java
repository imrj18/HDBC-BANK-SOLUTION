package com.ritik.customer_microservice.dto.accountDTO;

import com.ritik.customer_microservice.enums.AccountType;
import com.ritik.customer_microservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDTO {

    private UUID accountId;

    private Long accountNum;

    private AccountType accountType;

    private Long bankId;

    private Status accountStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
