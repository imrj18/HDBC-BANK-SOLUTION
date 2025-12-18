package com.ritik.bank_microservice.controller;



import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.service.BankService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/banks")
public class BankController {

    private final BankService service;

    public BankController(BankService service) {
        this.service = service;
    }

    //Register Bank
    @PostMapping
    public ResponseEntity<String> addBank(@Valid @RequestBody BankRequestDTO dto) {
        log.info("API call: POST /api/banks");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addBank(dto));
    }

    @GetMapping
    public ResponseEntity<List<BankResponseDTO>> getBanks(@RequestParam(required = false) String ifsc,
                                                          @RequestParam(required = false) Long id) {
        log.info("API call: POST /api/banks/getBanks");
        return ResponseEntity.ok(service.getBankDetails(ifsc, id));
    }

}
