package com.hdbc.dto.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class TransactionEvent implements Serializable {
    private UUID transactionId;
    private String email;
    private BigDecimal amount;
    private String operationType;
    private String transactionType;
    private String status;
    private String message;

    // getters & setters

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TransactionEvent() {
    }

    public TransactionEvent(UUID transactionId, String email, BigDecimal amount, String operationType,
                            String transactionType, String status, String message) {
        this.transactionId = transactionId;
        this.email = email;
        this.amount = amount;
        this.operationType = operationType;
        this.transactionType = transactionType;
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        return "TransactionEvent{" +
                "transactionId=" + transactionId +
                ", email='" + email + '\'' +
                ", amount=" + amount +
                ", operationType='" + operationType + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

