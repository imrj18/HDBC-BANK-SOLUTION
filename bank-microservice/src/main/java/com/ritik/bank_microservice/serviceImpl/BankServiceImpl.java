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
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
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
    private final ModelMapper modelMapper;

    private BankResponseDTO mapToDTO(Bank bank) {
        return modelMapper.map(bank, BankResponseDTO.class);
    }


    @Override
    public BankResponseDTO addBank(BankRequestDTO dto) {

        log.info("Attempting to add new bank | bankName={} | ifscCode={} | branch={}",
                dto.getBankName(), dto.getIfscCode(), dto.getBranch());

        if (repository.findByIfscCode(dto.getIfscCode()).isPresent()) {
            log.warn("Failed to add bank — IFSC code already exists | ifscCode={}", dto.getIfscCode());
            throw new IfscCodeAlreadyExistException("IFSC code already exists");
        }

        Bank bank = repository.save(new Bank(dto.getBankName(), dto.getIfscCode(), dto.getBranch()));

        log.info("Bank added successfully | bankId={} | ifscCode={}", bank.getBankId(), bank.getIfscCode());

        return mapToDTO(bank);
    }

    @Override
    @Cacheable(value = "banks",
            key = "{#ifsc, #bankId, #page, #size}",
            unless = "#result == null"
    )
    public PageResponse<BankResponseDTO> getBankDetails(String ifsc, Long bankId, int page, int size) {

        log.info("Fetching bank details | ifsc={} | bankId={} | page={} | size={}", ifsc, bankId, page, size);

        if (ifsc != null && bankId != null) {
            log.warn("Both IFSC and bankId provided | ifsc={} | bankId={}", ifsc, bankId);
            throw new BadRequestException("Provide either IFSC or bankId");
        }


        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        if (ifsc != null) {
            if (ifsc.length() != 11) {
                log.warn("Invalid IFSC code format | ifsc={}", ifsc);
                throw new InvalidIfscCodeException("Invalid IFSC Code");
            }

            Bank bank = repository.findByIfscCode(ifsc)
                    .orElseThrow(() -> {
                        log.warn("Bank not found with IFSC | ifsc={}", ifsc);
                        return new BankNotFoundException("Bank not found");
                    });
            log.info("Bank found by IFSC | ifsc={} | bankId={}", ifsc, bank.getBankId());
            return new PageResponse<>(List.of(mapToDTO(bank)), 0,1,1, true);
        }

        if (bankId != null) {
            Bank bank = repository.findById(bankId)
                    .orElseThrow(() -> {
                        log.warn("Bank not found with bankId | bankId={}", bankId);
                        return new BankNotFoundException("Bank not found");
                    });

            log.info("Bank found by bankId | bankId={} | ifsc={}", bankId, bank.getIfscCode());
            return new PageResponse<>(List.of(mapToDTO(bank)), 0, 1, 1, true);
        }

        Page<Bank> bankPage = repository.findAll(pageable);

        log.info("Returning paginated list of banks | page={} | size={} | total={}",
                bankPage.getNumber(), bankPage.getSize(), bankPage.getTotalElements());

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
        log.info("Fetching customers by IFSC | ifsc={} | minBalance={} | maxBalance={} | page={} | size={}",
                ifsc, minBalance, maxBalance, page, size);

        Bank bank = repository.findByIfscCode(ifsc).orElseThrow(() -> {
            log.warn("Invalid IFSC code provided | ifsc={}", ifsc);
            return new BankNotFoundException("Invalid IFSC");
        });

        PageResponse<CustomerBalanceDTO> customers;

        try {
            customers = customerClient.getCustomers(bank.getBankId(), minBalance, maxBalance, page, size);
        } catch (feign.FeignException.NotFound ex) {
            log.warn("No customers found for bankId={} | minBalance={} | maxBalance={}",
                    bank.getBankId(), minBalance, maxBalance);
            throw new CustomerNotFoundException("No customers found");
        }catch (feign.FeignException ex) {
            log.error("Customer service unavailable | bankId={}", bank.getBankId(), ex);
            throw new ServiceUnavailableException("Customer service is currently unavailable. Please try again later");
        }

        if (customers == null || customers.getData().isEmpty()) {
            log.warn("No customers returned by customer service for bankId={} | minBalance={} | maxBalance={}",
                    bank.getBankId(), minBalance, maxBalance);
            throw new CustomerNotFoundException("No customers found");
        }

        log.info("Successfully fetched {} customers for bankId={}", customers.getData().size(), bank.getBankId());
        return customers;
    }
}
