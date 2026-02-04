package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.accountDTO.AccountBalanceDTO;
import com.ritik.customer_microservice.dto.accountDTO.AccountResponseDTO;
import com.ritik.customer_microservice.dto.accountDTO.CreateAccountDTO;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.service.AccountService;
import com.ritik.customer_microservice.wrapper.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/customers/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create-account")
    public ResponseEntity<AccountResponseDTO> createAccount(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody CreateAccountDTO createAccountDTO) {

        String email = principal.getUsername();
        log.info("API call: CREATE ACCOUNT | user={}", email);

        AccountResponseDTO response =
                accountService.createAccount(email, createAccountDTO);

        log.info("Account created successfully | user={} | accountNum={}",
                email, response.getAccountNum());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountNum}/check-balance")
    public ResponseEntity<AccountBalanceDTO> checkBalance(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @PathVariable
            @NotNull(message = "Account number is required")
            @Positive(message = "Account number must be positive")
            Long accountNum) {

        String email = principal.getUsername();
        log.info("API call: CHECK BALANCE | user={} | accountNum={}", email, accountNum);

        AccountBalanceDTO balance =
                accountService.checkBalance(email, accountNum);

        log.info("Balance retrieved successfully | user={} | accountNum={}",
                email, accountNum);

        return ResponseEntity.ok(balance);
    }

    @GetMapping("/account-info")
    public ResponseEntity<PageResponse<AccountResponseDTO>> accountInfo(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestParam(required = false)
            @Positive(message = "Account number must be positive")
            Long accountNum) {

        String email = principal.getUsername();

        if (accountNum != null) {
            log.info("API call: ACCOUNT INFO | user={} | accountNum={}", email, accountNum);
        } else {
            log.info("API call: ACCOUNT INFO | user={} | all accounts", email);
        }

        PageResponse<AccountResponseDTO> accounts =
                accountService.getAccountInfo(email, accountNum);

        log.info("Account info retrieved | user={} | resultCount={}",
                email, accounts.getTotalPages());

        return ResponseEntity.ok(accounts);
    }
}
