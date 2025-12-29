package com.ritik.customer_microservice.dto.transactionDTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDTO {

    private String status;
    private String message;

    private UUID transactionReferenceId;

    private Long fromAccountNum;
    private Long toAccountNum;

    private BigDecimal amount;

    private BigDecimal senderClosingBalance;

    private LocalDateTime timestamp;
}

