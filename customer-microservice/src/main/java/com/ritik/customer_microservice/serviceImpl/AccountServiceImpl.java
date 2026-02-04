package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.accountDTO.AccountBalanceDTO;
import com.ritik.customer_microservice.dto.accountDTO.AccountResponseDTO;
import com.ritik.customer_microservice.dto.accountDTO.CreateAccountDTO;
import com.ritik.customer_microservice.dto.external.BankResponseDTO;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.exception.*;
import com.ritik.customer_microservice.feign.BankClient;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.AccountRepository;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.service.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
        log.info("Create account request | email={} | ifsc={}", email, dto.getIfscCode());

        PageResponse<BankResponseDTO> banks;

        try {
            banks = bankClient.getBanks(dto.getIfscCode(), null);
            log.debug("Bank service response received | ifsc={}", dto.getIfscCode());
        } catch (feign.FeignException.NotFound ex) {
            log.warn("Invalid IFSC code | ifsc={}", dto.getIfscCode());
            throw new BankNotFoundException("Invalid IFSC code");
        } catch (feign.FeignException ex) {
            log.error("Bank service unavailable | ifsc={}", dto.getIfscCode(), ex);
            throw new ServiceUnavailableException("Bank service is currently unavailable. Please try again later");
        }

        if (banks == null || banks.getData().isEmpty()) {
            log.warn("No bank found for IFSC | ifsc={}", dto.getIfscCode());
            throw new BankNotFoundException("Invalid IFSC code");
        }

        Long bankId = banks.getData().get(0).getBankId();

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Customer not found | email={}", email);
                    return new CustomerNotFoundException("Customer not found");
                });

        // Optional: prevent duplicate account
        // if (accountRepository.existsByCustomerAndBankId(customer, bankId)) {
        //     throw new ConflictException("Account already exists for this bank");
        // }

        Account account = toEntity(dto, bankId);
        account.setCustomer(customer);
        account.setAccountNum(accountNumberGenerator.generate(bankId));

        accountRepository.save(account);

        log.info(
                "Account created successfully | email={} | accountNum={} | bankId={}",
                email,
                account.getAccountNum(),
                bankId
        );

        return toResponseDto(account);
    }

    @Override
    @Cacheable(
            value = "checkBalance",
            key = "{#email, #accountNum}",
            unless = "#result == null"
    )
    public AccountBalanceDTO checkBalance(String email, Long accountNum){
        log.info("Check balance request | email={} | accountNum={}", email, accountNum);
        Customer customer = customerRepository.findByEmail(email).orElseThrow(()->{
            log.warn("Customer not found during balance check | email={}", email);
            return new CustomerNotFoundException("Customer not found");
        });
        Account account = accountRepository.findByAccountNumAndCustomer_CustomerId(accountNum,customer.getCustomerId())
                .orElseThrow(() -> {
                    log.warn("Account not found during balance check | email={} | accountNum={}", email, accountNum);
                    return new AccountNotFoundException("Account not found");
                });

        log.debug("Balance fetched | accountNum={} | balance={}", accountNum, account.getAmount());
        AccountBalanceDTO dto = new AccountBalanceDTO();
        dto.setAccountNumber(account.getAccountNum());
        dto.setAccountBalance(account.getAmount());
        return dto;
    }

    @Override
    @Cacheable(
            value = "accountInfo",
            key = "{#email, #accountNum}",
            unless = "#result == null"
    )
    public PageResponse<AccountResponseDTO> getAccountInfo(String email, Long accountNum) {
        log.info("Get account info request | email={} | accountNum={}", email, accountNum);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Customer not found during account info fetch | email={}", email);
                    return new CustomerNotFoundException("Customer not found");
                });
        List<AccountResponseDTO> responseList = new ArrayList<>();

        if (accountNum == null) {
            List<Account> accounts = accountRepository.findByCustomer_CustomerId(customer.getCustomerId());

            if (accounts.isEmpty()) {
                log.warn("No accounts found | email={}", email);
                throw new AccountNotFoundException("Account not found.");
            }

            for (Account account : accounts) {
                responseList.add(toResponseDto(account));
            }
            log.info("Fetched all accounts | email={} | count={}", email, responseList.size());

        } else {
            Account account = accountRepository
                    .findByAccountNumAndCustomer_CustomerId(accountNum, customer.getCustomerId())
                    .orElseThrow(() -> {
                        log.warn("Account not found | email={} | accountNum={}", email, accountNum);
                        return new AccountNotFoundException("Account not found");
                    });
            responseList.add(toResponseDto(account));
            log.info("Fetched account info | email={} | accountNum={}", email, accountNum);
        }

        return new PageResponse<>(
                responseList,
                0,
                1,
                responseList.size(),
                true
        );
    }

}
