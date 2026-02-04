package com.ritik.customer_microservice.dto.event;

import com.ritik.customer_microservice.enums.OperationType;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {
    private UUID transactionId;
    private String email;
    private BigDecimal amount;
    private OperationType operationType;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String message;
}
