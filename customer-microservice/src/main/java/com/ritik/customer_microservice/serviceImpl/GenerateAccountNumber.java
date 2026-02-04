package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.model.AccountNumberGenerator;
import com.ritik.customer_microservice.repository.AccountNumberGeneratorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateAccountNumber {
    private static final long MULTIPLIER = 1_000_000_000L;

    private final AccountNumberGeneratorRepository repository;

    @Transactional
    public Long generate(Long bankId) {
        log.info("Generating account number | bankId={}", bankId);

        AccountNumberGenerator sequence = repository.save(new AccountNumberGenerator());

        Long accountNumber = bankId * MULTIPLIER + sequence.getId();
        log.debug(
                "Account number generated | bankId={} | sequenceId={} | accountNumber={}",
                bankId,
                sequence.getId(),
                accountNumber
        );

        return accountNumber;
    }
}

