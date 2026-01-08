package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.accountDTO.AccountBalanceDTO;
import com.ritik.customer_microservice.dto.accountDTO.AccountResponseDTO;
import com.ritik.customer_microservice.dto.accountDTO.CreateAccountDTO;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create-account")
    public ResponseEntity<AccountResponseDTO> createAccount(@AuthenticationPrincipal CustomerPrincipal principal,
                                                            @Valid @RequestBody CreateAccountDTO createAccountDTO){
        String email = principal.getUsername();
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(email,createAccountDTO));
    }

    @GetMapping("/{accountNum}/check-balance")
    public ResponseEntity<AccountBalanceDTO> checkBalance(@AuthenticationPrincipal CustomerPrincipal principal,
                                                          @PathVariable @NotNull(message = "Account number is required")
                                                          @Positive(message = "Account number must be positive")
                                                          Long accountNum) {
        String email = principal.getUsername();
        return ResponseEntity.status(HttpStatus.OK).body(accountService.checkBalance(email,accountNum));
    }

    @GetMapping("/account-info")
    public ResponseEntity<List<AccountResponseDTO>> accountInfo(@AuthenticationPrincipal CustomerPrincipal principal,
                                                                @RequestParam(required = false)
                                                                @Positive(message = "Account number must be positive")
                                                                Long accountNum){
        String email = principal.getUsername();
        return ResponseEntity.ok(accountService.getAccountInfo(email,accountNum));
    }
}
