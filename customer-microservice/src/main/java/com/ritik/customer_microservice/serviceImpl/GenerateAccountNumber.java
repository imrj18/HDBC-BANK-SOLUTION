package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.model.AccountNumberGenerator;
import com.ritik.customer_microservice.repository.AccountNumberGeneratorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerateAccountNumber {
    private static final long MULTIPLIER = 1_000_000_000L;

    private final AccountNumberGeneratorRepository repository;

    @Transactional
    public Long generate(Long bankId) {

        AccountNumberGenerator sequence = repository.save(new AccountNumberGenerator());

        return bankId * MULTIPLIER + sequence.getId();
    }
}

