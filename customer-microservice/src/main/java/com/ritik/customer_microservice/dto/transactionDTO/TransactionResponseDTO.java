package com.ritik.customer_microservice.dto.transactionDTO;

import com.ritik.customer_microservice.enums.OperationType;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.enums.TransactionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class TransactionResponseDTO {
    private UUID transactionId;
    private Long accountNum;
    private OperationType operationType;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal closingBalance;
    private TransactionStatus transactionStatus;
    private LocalDateTime createdAt;
}
