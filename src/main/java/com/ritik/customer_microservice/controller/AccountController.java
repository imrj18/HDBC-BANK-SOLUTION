package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.AccountBalanceDTO;
import com.ritik.customer_microservice.dto.AccountResponseDTO;
import com.ritik.customer_microservice.dto.CreateAccountDTO;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.serviceImpl.AccountServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/customers/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountServiceImpl accountService;

    @PostMapping("/create-account")
    public ResponseEntity<AccountResponseDTO> createAccount(@AuthenticationPrincipal CustomerPrincipal principal,
                                                            @Valid @RequestBody CreateAccountDTO createAccountDTO){
        String email = principal.getUsername();
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(email,createAccountDTO));
    }

    @GetMapping("/{account_num}/check-balance")
    public ResponseEntity<AccountBalanceDTO> checkBalance(@AuthenticationPrincipal CustomerPrincipal principal,
                                                          @PathVariable Long account_num) throws AccessDeniedException {
        String email = principal.getUsername();
        return ResponseEntity.status(HttpStatus.OK).body(accountService.checkBalance(email,account_num));
    }

    @GetMapping("/account-info")
    public ResponseEntity<List<AccountResponseDTO>> accountInfo(@AuthenticationPrincipal CustomerPrincipal principal,
                                                                @RequestParam(required = false) Long accountNum)
                                                                throws AccessDeniedException {
        String email = principal.getUsername();
        return ResponseEntity.ok(accountService.getAccpuntInfo(email,accountNum));
    }
}
