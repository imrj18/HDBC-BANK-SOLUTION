package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.model.AccountNumberGenerator;
import com.ritik.customer_microservice.repository.AccountNumberGeneratorRepository;
import com.ritik.customer_microservice.serviceImpl.GenerateAccountNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateAccountNumberTest {

    @Mock
    private AccountNumberGeneratorRepository repository;

    @InjectMocks
    private GenerateAccountNumber generator;

    @Test
    void shouldGenerateAccountNumberSuccessfully() {

        // Arrange
        Long bankId = 12L;

        AccountNumberGenerator savedEntity = new AccountNumberGenerator();
        savedEntity.setId(45L);

        Mockito.when(repository.save(Mockito.any(AccountNumberGenerator.class)))
                .thenReturn(savedEntity);

        // Act
        Long accountNumber = generator.generate(bankId);

        // Assert
        Long expected = bankId * 1_000_000_000L + 45L;
        Assertions.assertEquals(expected, accountNumber);

        Mockito.verify(repository).save(Mockito.any(AccountNumberGenerator.class));
    }
}

