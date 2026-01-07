package com.ritik.customer_microservice.service;

import com.ritik.customer_microservice.dto.transactionDTO.*;

import java.util.List;

public interface TransactionService {
    TransactionResponseDTO depositMoney(String email, DepositRequestDTO depositRequestDTO);

    TransactionResponseDTO withdrawMoney(String email, WithdrawRequestDTO withdrawRequestDTO);

    List<TransactionHistoryDTO> transactionHistory(String email, Long accountNum);

    TransferResponseDTO transferMoney(String email, TransferRequestDTO transferRequestDTO);
}
