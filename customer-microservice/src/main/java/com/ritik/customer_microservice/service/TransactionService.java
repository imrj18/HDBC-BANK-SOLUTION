package com.ritik.customer_microservice.service;

import com.ritik.customer_microservice.dto.transactionDTO.*;
import com.ritik.customer_microservice.wrapper.PageResponse;

public interface TransactionService {
    TransactionResponseDTO depositMoney(String email, DepositRequestDTO depositRequestDTO);

    TransactionResponseDTO withdrawMoney(String email, WithdrawRequestDTO withdrawRequestDTO);

    PageResponse<TransactionHistoryDTO> transactionHistory(String email, Long accountNum, int page, int size);

    TransferResponseDTO transferMoney(String email, TransferRequestDTO transferRequestDTO);

    TransactionResponseDTO transactionConfirm(String email, ConfirmRequestDTO dto);
}
