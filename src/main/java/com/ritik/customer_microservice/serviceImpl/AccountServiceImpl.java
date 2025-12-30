package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.accountDTO.AccountBalanceDTO;
import com.ritik.customer_microservice.dto.accountDTO.AccountResponseDTO;
import com.ritik.customer_microservice.dto.accountDTO.CreateAccountDTO;
import com.ritik.customer_microservice.dto.external.BankResponseDTO;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.exception.AccountAccessDeniedException;
import com.ritik.customer_microservice.exception.AccountNotFoundException;
import com.ritik.customer_microservice.exception.BankNotFoundException;
import com.ritik.customer_microservice.exception.CustomerNotFoundException;
import com.ritik.customer_microservice.feign.BankClient;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.AccountRepository;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.service.AccountService;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final CustomerRepository customerRepository;

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final GenerateAccountNumber accountNumberGenerator;

    private final BankClient bankClient;

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

    private Account toEntity(CreateAccountDTO dto, Long bankId) {
        Account account = new Account();

        account.setAccountType(dto.getAccountType());
        account.setBankId(bankId);
        account.setPinHash(passwordEncoder.encode(dto.getPin()));
        account.setAccountStatus(Status.ACTIVE);
        account.setAmount(BigDecimal.ZERO);

        return account;
    }

    @Override
    @Transactional
    public AccountResponseDTO createAccount(String email, CreateAccountDTO dto) {

        List<BankResponseDTO> banks;

        try {
            banks = bankClient.getBanks(dto.getIfscCode(), null);
        } catch (feign.FeignException.NotFound ex) {
            throw new BankNotFoundException("Invalid IFSC code");
        } catch (feign.FeignException ex) {
            throw new ServiceUnavailableException(
                    "Bank service is currently unavailable. Please try again later"
            );
        }

        if (banks == null || banks.isEmpty()) {
            throw new BankNotFoundException("Invalid IFSC code");
        }

        Long bankId = banks.get(0).getBankId();

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Optional: prevent duplicate account
        // if (accountRepository.existsByCustomerAndBankId(customer, bankId)) {
        //     throw new ConflictException("Account already exists for this bank");
        // }

        Account account = toEntity(dto, bankId);
        account.setCustomer(customer);
        account.setAccountNum(accountNumberGenerator.generate(bankId));

        accountRepository.save(account);

        return toResponseDto(account);
    }



    @Override
    public AccountBalanceDTO checkBalance(String email, Long accountNum){
        Customer customer = customerRepository.findByEmail(email).orElseThrow(()->
                new CustomerNotFoundException("Customer not found"));
        Account account = accountRepository.findByAccountNumAndCustomer_CustomerId(accountNum,customer.getCustomerId())
                .orElseThrow(()->new AccountAccessDeniedException("You are not authorized to access this account"));

        AccountBalanceDTO dto = new AccountBalanceDTO();
        dto.setAccountNumber(account.getAccountNum());
        dto.setAccountBalance(account.getAmount());
        return dto;
    }

    @Override
    public List<AccountResponseDTO> getAccountInfo(String email, Long accountNum) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        if (accountNum == null) {
            List<Account> accounts = accountRepository.findByCustomer_CustomerId(customer.getCustomerId());

            if (accounts.isEmpty()) {
                throw new AccountNotFoundException("Account not found.");
            }

            return accounts.stream().map(this::toResponseDto).toList();
        }

        Account account = accountRepository
                .findByAccountNumAndCustomer_CustomerId(accountNum, customer.getCustomerId())
                .orElseThrow(() -> new AccountAccessDeniedException("You are not authorized to access this account"));

        return List.of(toResponseDto(account));
    }
}
