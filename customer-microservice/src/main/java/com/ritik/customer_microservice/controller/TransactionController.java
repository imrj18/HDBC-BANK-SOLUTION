package com.ritik.customer_microservice.controller;

import com.ritik.customer_microservice.dto.transactionDTO.*;
import com.ritik.customer_microservice.model.CustomerPrincipal;
import com.ritik.customer_microservice.service.TransactionService;
import com.ritik.customer_microservice.serviceImpl.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> depositMoney(@AuthenticationPrincipal CustomerPrincipal principal,
                                                               @Valid @RequestBody DepositRequestDTO depositRequestDTO){
        String email = principal.getUsername();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(transactionService.
                depositMoney(email,depositRequestDTO));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> withdrawMoney(@AuthenticationPrincipal CustomerPrincipal principal,
                                                               @Valid @RequestBody WithdrawRequestDTO withdrawRequestDTO){
        String email = principal.getUsername();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(transactionService.
                withdrawMoney(email,withdrawRequestDTO));
    }

    @GetMapping("/transactionHistory")
    public ResponseEntity<PageResponse<TransactionHistoryDTO>> history(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestParam(required = false) Long accountNum,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "5") int size){

        String email = principal.getUsername();
        return ResponseEntity.ok(transactionService.transactionHistory(email, accountNum, page, size));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transferMoney(@AuthenticationPrincipal CustomerPrincipal principal,
                                                             @Valid @RequestBody TransferRequestDTO transferRequestDTO){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(transactionService.transferMoney(principal.getUsername(),
                transferRequestDTO));
    }
}
