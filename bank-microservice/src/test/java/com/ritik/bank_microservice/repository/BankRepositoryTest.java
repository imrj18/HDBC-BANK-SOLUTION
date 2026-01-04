package com.ritik.bank_microservice.repository;

import com.ritik.bank_microservice.model.Bank;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
class BankRepositoryTest {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindBankByIfscCode() {

        // Arrange
        Bank bank = new Bank();
        bank.setBankName("HDBC Bank");
        bank.setIfscCode("HDBC0000101");
        bank.setBranch("Mumbai");

        entityManager.persist(bank);
        entityManager.flush();

        // Act
        Optional<Bank> result = bankRepository.findByIfscCode("HDBC0000101");

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("HDBC Bank", result.get().getBankName());
        Assertions.assertEquals("Mumbai", result.get().getBranch());
    }

    @Test
    void shouldReturnEmptyWhenIfscCodeNotFound() {

        // Act
        Optional<Bank> result = bankRepository.findByIfscCode("INVALID0001");

        // Assert
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldFailWhenDuplicateIfscCodeInserted() {

        //Arrange
        Bank bank1 = new Bank("HDBC Bank", "HDBC0000102", "Mumbai");
        Bank bank2 = new Bank("Another Bank", "HDBC0000102", "Delhi");

        //Act
        entityManager.persist(bank1);
        entityManager.flush();

        //Assery
        Assertions.assertThrows(PersistenceException.class, () -> {
                entityManager.persist(bank2);
                entityManager.flush();
            }
        );
    }

    @Test
    void shouldFailWhenIfscCodeIsNull() {

        //Act
        Bank bank = new Bank("HDBC Bank", null, "Mumbai");

        //Act + Assert
        Assertions.assertThrows(PersistenceException.class, () -> {
                    entityManager.persist(bank);
                    entityManager.flush();
                }
        );
    }

    @Test
    void shouldFailWhenBankNameIsNull() {

        //Arrange
        Bank bank = new Bank(null, "HDBC0000103", "Mumbai");

        //Act + Assert
        Assertions.assertThrows(PersistenceException.class, () -> {
                    entityManager.persist(bank);
                    entityManager.flush();
                }
        );
    }

    @Test
    void shouldFailWhenBranchIsNull() {

        //Arrange
        Bank bank = new Bank("HDBC Bank", "HDBC0000104", null);

        //Act + Assert
        Assertions.assertThrows(PersistenceException.class, () -> {
                    entityManager.persist(bank);
                    entityManager.flush();
                }
        );
    }

    @Test
    void shouldSetCreatedAndUpdatedAtOnPersist() {

        //Arrange
        Bank bank = new Bank("HDBC Bank", "HDBC0000105", "Mumbai");

        //Act
        entityManager.persist(bank);
        entityManager.flush();

        //Assert
        Assertions.assertNotNull(bank.getCreatedAt());
        Assertions.assertNotNull(bank.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAtOnUpdate() {

        //Arrange
        Bank bank = new Bank("HDBC Bank", "HDBC0000106", "Mumbai");

        //Act
        entityManager.persist(bank);
        entityManager.flush();

        LocalDateTime createdUpdatedAt = bank.getUpdatedAt();

        bank.setBranch("Delhi");
        entityManager.merge(bank);
        entityManager.flush();

        //Assert
        Assertions.assertTrue(bank.getUpdatedAt().isAfter(createdUpdatedAt));
    }

    @Test
    void shouldGenerateBankIdOnPersist() {

        //Arrange
        Bank bank = new Bank("HDBC Bank", "HDBC0000107", "Mumbai");

        //Assert
        Assertions.assertNull(bank.getBankId()); // before persist

        //Act
        entityManager.persist(bank);
        entityManager.flush();

        //Assert
        Assertions.assertNotNull(bank.getBankId()); // after persist
    }

}
