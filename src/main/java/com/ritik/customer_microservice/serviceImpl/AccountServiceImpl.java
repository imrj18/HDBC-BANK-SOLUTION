package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.*;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.exception.AccountNotFoundException;
import com.ritik.customer_microservice.exception.CustomerNotFoundException;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.AccountRepository;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final CustomerRepository customerRepository;

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final GenerateAccountNumber accountNumber;

    private AccountResponseDTO toResponseDto(Account account) {

        AccountResponseDTO dto = new AccountResponseDTO();

        dto.setAccountId(account.getAccountId());
        dto.setAccountNum(account.getAccountNum());
        dto.setBankId(account.getBankId());
        dto.setAccountType(account.getAccountType());
        dto.setAccountStatus(account.getAccountStatus());

        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());

        return dto;
    }

    private static Account toEntity(CreateAccountDTO dto, PasswordEncoder passwordEncoder) {
        Account account = new Account();

        account.setAccountType(dto.getAccountType());
        account.setBankId(dto.getBankId());
        account.setPinHash(passwordEncoder.encode(dto.getPin()));

        account.setAccountStatus(Status.ACTIVE);

        return account;
    }

    public AccountResponseDTO createAccount(String email, CreateAccountDTO createAccountDTO){
        Customer customer = customerRepository.findByEmail(email).orElseThrow(()->
                new CustomerNotFoundException("Customer not found"));

        Account account = toEntity(createAccountDTO, passwordEncoder);

        account.setCustomerId(customer.getCustomerId());

        account.setAccountNum(accountNumber.generate(createAccountDTO.getBankId()));

        accountRepository.save(account);

        return toResponseDto(account);
    }

    public AccountBalanceDTO checkBalance(String email, Long accountNum){
        Customer customer = customerRepository.findByEmail(email).orElseThrow(()->
                new CustomerNotFoundException("Customer not found"));
        Account account = accountRepository.findByAccountNumAndCustomerId(accountNum,customer.getCustomerId())
                .orElseThrow(()->new AccountNotFoundException("Unauthorized"));

        AccountBalanceDTO dto = new AccountBalanceDTO();
        dto.setAccountNumber(account.getAccountNum());
        dto.setAccountBalance(account.getAmount());
        return dto;
    }

    public List<AccountResponseDTO> getAccpuntInfo(String email, Long accountNum) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        if (accountNum == null) {
            List<Account> accounts = accountRepository.findByCustomerId(customer.getCustomerId());

            if (accounts.isEmpty()) {
                throw new AccountNotFoundException("Account not found.");
            }

            return accounts.stream().map(this::toResponseDto).toList();
        }

        Account account = accountRepository
                .findByAccountNumAndCustomerId(accountNum, customer.getCustomerId())
                .orElseThrow(() -> new AccountNotFoundException("Unauthorized"));

        return List.of(toResponseDto(account));
    }
}
