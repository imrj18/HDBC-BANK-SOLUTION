package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.enums.*;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.Transaction;
import com.ritik.customer_microservice.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer customer;
    private Account account1;
    private Account account2;
    private Pageable pageable;

    @BeforeEach
    void setup() {

        Pageable pageable = PageRequest.of(0, 10);

        customer = new Customer();
        customer.setName("Ritik Jani");
        customer.setEmail("ritik@gmail.com");
        customer.setPhone("9876543210");
        customer.setAadhar("123456789012");
        customer.setAddress("Bhilai");
        customer.setPasswordHash("hashed");
        customer.setStatus(Status.ACTIVE);

        entityManager.persist(customer);

        account1 = new Account();
        account1.setAccountNum(111122223333L);
        account1.setAmount(new BigDecimal("10000"));
        account1.setAccountType(AccountType.Saving);
        account1.setAccountStatus(Status.ACTIVE);
        account1.setPinHash("1234");
        account1.setBankId(1L);
        account1.setCustomer(customer);

        entityManager.persist(account1);

        account2 = new Account();
        account2.setAccountNum(444455556666L);
        account2.setAmount(new BigDecimal("8000"));
        account2.setAccountType(AccountType.Current);
        account2.setAccountStatus(Status.ACTIVE);
        account2.setPinHash("5678");
        account2.setBankId(1L);
        account2.setCustomer(customer);

        entityManager.persist(account2);

        entityManager.persist(createTransaction(
                account1, account1.getAccountNum(), 1L,
                new BigDecimal("1000"), new BigDecimal("11000")
        ));

        entityManager.persist(createTransaction(
                account1, account1.getAccountNum(), 1L,
                new BigDecimal("2000"), new BigDecimal("9000")
        ));

        entityManager.persist(createTransaction(
                account2, account2.getAccountNum(), 1L,
                new BigDecimal("3000"), new BigDecimal("11000")
        ));

        entityManager.flush();
    }

    private Transaction createTransaction(
            Account account,
            Long accountNum,
            Long bankId,
            BigDecimal amount,
            BigDecimal closingBalance
    ) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAccountNum(accountNum);
        tx.setBankId(bankId);
        tx.setTransactionType(TransactionType.CREDIT);
        tx.setOperationType(OperationType.DEPOSIT);
        tx.setAmount(amount);
        tx.setClosingBalance(closingBalance);
        tx.setTransactionStatus(TransactionStatus.SUCCESS);
        return tx;
    }

    @Test
    void shouldFindTransactionsByAccountId() {

        Page<Transaction> transactions = transactionRepository.findByAccount_AccountId(account1.getAccountId(),pageable);

        Assertions.assertEquals(2, transactions.getContent().size());
        for (Transaction transaction : transactions) {
            Assertions.assertEquals(account1.getAccountId(), transaction.getAccount().getAccountId());
        }

    }

    @Test
    void shouldFindTransactionsByAccountIdIn() {

        Page<Transaction> transactions = transactionRepository
                .findByAccount_AccountIdIn(List.of(account1.getAccountId(), account2.getAccountId()),pageable);

        Assertions.assertEquals(3, transactions.getContent().size());
    }

    @Test
    void shouldReturnEmptyWhenAccountIdNotFound() {

        Page<Transaction> transactions = transactionRepository.findByAccount_AccountId(UUID.randomUUID(),pageable);

        Assertions.assertTrue(transactions.isEmpty());
    }
}
