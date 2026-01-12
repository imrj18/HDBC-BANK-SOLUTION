package com.ritik.bank_microservice.serviceImpl;


import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import com.ritik.bank_microservice.exception.*;
import com.ritik.bank_microservice.feign.CustomerClient;
import com.ritik.bank_microservice.model.Bank;
import com.ritik.bank_microservice.repository.BankRepository;
import com.ritik.bank_microservice.service.BankService;
import com.ritik.bank_microservice.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final BankRepository repository;
    private final CustomerClient customerClient;

    private BankResponseDTO mapToDTO(Bank bank) {
        return new BankResponseDTO(
                bank.getBankId(),
                bank.getBankName(),
                bank.getIfscCode(),
                bank.getBranch(),
                bank.getCreatedAt(),
                bank.getUpdatedAt()
        );
    }


    @Override
    public BankResponseDTO addBank(BankRequestDTO dto) {

        if (repository.findByIfscCode(dto.getIfscCode()).isPresent()) {
            throw new IfscCodeAlreadyExistException("IFSC code already exists");
        }

        Bank bank = repository.save(new Bank(dto.getBankName(), dto.getIfscCode(), dto.getBranch()));

        return mapToDTO(bank);
    }

    @Override
    public PageResponse<BankResponseDTO> getBankDetails(String ifsc, Long bankId, int page, int size) {

        if (ifsc != null && bankId != null) {
            throw new BadRequestException("Provide either IFSC or bankId");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        if (ifsc != null) {
            if (ifsc.length() != 11) {
                throw new InvalidIfscCodeException("Invalid IFSC Code");
            }

            Bank bank = repository.findByIfscCode(ifsc).orElseThrow(() -> new BankNotFoundException("Bank not found"));

            return new PageResponse<>(List.of(mapToDTO(bank)), 0,1,1, true);
        }

        if (bankId != null) {
            Bank bank = repository.findById(bankId).orElseThrow(() -> new BankNotFoundException("Bank not found"));

            return new PageResponse<>(List.of(mapToDTO(bank)), 0,1,1, true);

        }

        Page<Bank> bankPage = repository.findAll(pageable);

        return new PageResponse<>(
                bankPage.getContent()
                        .stream()
                        .map(this::mapToDTO)
                        .toList(),
                bankPage.getNumber(),
                bankPage.getTotalPages(),
                bankPage.getTotalElements(),
                bankPage.isLast()
        );
    }

    @Override
    public PageResponse<CustomerBalanceDTO> getCustomersByIfsc(String ifsc,
                                                       BigDecimal minBalance,
                                                       BigDecimal maxBalance,
                                                       int page,
                                                       int size) {

        Bank bank = repository.findByIfscCode(ifsc).orElseThrow(() -> new BankNotFoundException("Invalid IFSC"));

        PageResponse<CustomerBalanceDTO> customers;

        try {
            customers = customerClient.getCustomers(
                    bank.getBankId(), minBalance, maxBalance, page, size);
        } catch (feign.FeignException.NotFound ex) {
            throw new CustomerNotFoundException("No customers found");
        }


        if (customers == null || customers.getData().isEmpty()) {
            throw new CustomerNotFoundException("No customers found");
        }

        return customers;
    }
}
