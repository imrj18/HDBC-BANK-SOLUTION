package com.ritik.bank_microservice.serviceImpl;


import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.exception.BadRequestException;
import com.ritik.bank_microservice.exception.ResourceNotFoundException;
import com.ritik.bank_microservice.model.Bank;
import com.ritik.bank_microservice.repository.BankRepository;
import com.ritik.bank_microservice.service.BankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BankServiceImpl implements BankService {

    private final BankRepository repository;

    public BankServiceImpl(BankRepository repository) {

        this.repository = repository;
    }

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
        log.info("Request received to add bank.");
        if(dto.getIfscCode().length() != 11){
            log.warn("Bank creation failed. IFSC code invalid: {}", dto.getIfscCode());
            throw new BadRequestException("Invalid IFSC Code");
        }
        if(repository.findByIfscCode(dto.getIfscCode()).isPresent()){
            log.warn("Bank creation failed. IFSC already exists: {}", dto.getIfscCode());
            throw new BadRequestException("Ifsc code already exists.");
        }
        if(dto.getBankName().isEmpty() || dto.getBranch().isEmpty()){
            log.warn("Bank creation failed. All Fields Required.");
            throw new BadRequestException("All Fields Required");
        }

        Bank bank = new Bank(dto.getBankName(), dto.getIfscCode(), dto.getBranch());
        repository.save(bank);
        log.info("Bank created successfully with IFSC: {}", dto.getIfscCode());
        return mapToDTO(bank);
    }

    @Override
    public List<BankResponseDTO> getBankDetails(String ifsc, Long bank_id) {
        log.info("Fetching bank details. IFSC: {}, ID: {}", ifsc, bank_id);
        if (ifsc != null && bank_id != null) {
            log.warn("Invalid request: Both IFSC and ID provided");
            throw new BadRequestException("Please provide either IFSC or ID, not both");
        }

        if (ifsc != null && !ifsc.isBlank()) {
            Bank bank = repository.findByIfscCode(ifsc).orElseThrow(() ->{
                log.warn("Bank not found for IFSC: {}", ifsc);
                return  new ResourceNotFoundException("Bank not found with IFSC " + ifsc);
            });
            log.info("Bank found for IFSC: {}", ifsc);
            return List.of(mapToDTO(bank));
        }

        if (bank_id != null) {
            Bank bank = repository.findById(bank_id).orElseThrow(() -> {
                log.warn("Bank not found for ID: {}", bank_id);
                return  new ResourceNotFoundException("Bank not found with id " + bank_id);
            });
            log.info("Bank found for ID: {}", bank_id);
            return List.of(mapToDTO(bank));
        }

        // No filters → return all
        log.info("Fetching all banks");
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

}

