package com.ritik.customer_microservice.service;

import com.ritik.customer_microservice.dto.AccountBalanceDTO;
import com.ritik.customer_microservice.dto.AccountResponseDTO;
import com.ritik.customer_microservice.dto.CreateAccountDTO;

import java.util.List;

public interface AccountService {

    AccountResponseDTO createAccount(String email, CreateAccountDTO createAccountDTO);

    AccountBalanceDTO checkBalance(String email, Long accountNum);

    List<AccountResponseDTO> getAccountInfo(String email, Long accountNum);
}
