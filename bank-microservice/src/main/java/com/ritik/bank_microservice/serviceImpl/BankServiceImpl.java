package com.ritik.bank_microservice.serviceImpl;


import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import com.ritik.bank_microservice.exception.*;
import com.ritik.bank_microservice.feign.CustomerClient;
import com.ritik.bank_microservice.model.Bank;
import com.ritik.bank_microservice.repository.BankRepository;
import com.ritik.bank_microservice.service.BankService;
import jakarta.ws.rs.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            throw new ConflictException("IFSC code already exists");
        }

        Bank bank = repository.save(new Bank(dto.getBankName(), dto.getIfscCode(), dto.getBranch()));

        return mapToDTO(bank);
    }

    @Override
    public List<BankResponseDTO> getBankDetails(String ifsc, Long bankId) {

        if (ifsc != null && bankId != null) {
            throw new BadRequestException("Provide either IFSC or bankId");
        }

        if (ifsc != null) {
            if (ifsc.length() != 11) {
                throw new BadRequestException("Invalid IFSC Code");
            }

            Bank bank = repository.findByIfscCode(ifsc).orElseThrow(() -> new ResourceNotFoundException("Bank not found"));

            return List.of(mapToDTO(bank));
        }

        if (bankId != null) {
            Bank bank = repository.findById(bankId).orElseThrow(() -> new ResourceNotFoundException("Bank not found"));

            return List.of(mapToDTO(bank));
        }

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<CustomerBalanceDTO> getCustomersByIfsc(String ifsc,
                                                       BigDecimal minBalance,
                                                       BigDecimal maxBalance) {

        Bank bank = repository.findByIfscCode(ifsc)
                .orElseThrow(() -> new BankNotFoundException("Invalid IFSC"));

        List<CustomerBalanceDTO> customers;

        try {
            customers = customerClient.getCustomers(
                    bank.getBankId(), minBalance, maxBalance
            );
        } catch (feign.FeignException.NotFound ex) {
            throw new CustomerNotFoundException("No customers found");
        }


        if (customers == null || customers.isEmpty()) {
            throw new CustomerNotFoundException("No customers found");
        }

        return customers;
    }
}
