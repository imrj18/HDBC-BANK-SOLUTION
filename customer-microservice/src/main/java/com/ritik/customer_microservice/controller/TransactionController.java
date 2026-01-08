package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.transactionDTO.*;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.service.TransactionService;
import com.ritik.customer_microservice.serviceImpl.PageResponse;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> depositMoney(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody DepositRequestDTO depositRequestDTO) {

        String email = principal.getUsername();
        log.info("API call: DEPOSIT MONEY | user={}", email);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(transactionService.depositMoney(email, depositRequestDTO));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> withdrawMoney(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody WithdrawRequestDTO withdrawRequestDTO) {

        String email = principal.getUsername();
        log.info("API call: WITHDRAW MONEY | user={}", email);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(transactionService.withdrawMoney(email, withdrawRequestDTO));
    }

    @GetMapping("/transactionHistory")
    public ResponseEntity<PageResponse<TransactionHistoryDTO>> history(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestParam(required = false)
            @Positive(message = "Account number must be positive")
            Long accountNum,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        String email = principal.getUsername();

        if (accountNum != null) {
            log.info("API call: TRANSACTION HISTORY | user={} | accountNum={}", email, accountNum);
        } else {
            log.info("API call: TRANSACTION HISTORY | user={} | all accounts", email);
        }

        return ResponseEntity.ok(transactionService.transactionHistory(email, accountNum, page, size)
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transferMoney(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody TransferRequestDTO transferRequestDTO) {

        String email = principal.getUsername();
        log.info("API call: TRANSFER MONEY | user={}", email);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(transactionService.transferMoney(email, transferRequestDTO));
    }
}
