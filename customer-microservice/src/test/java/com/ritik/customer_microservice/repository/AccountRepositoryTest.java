package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.enums.AccountType;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setup() {

        customer = new Customer();
        customer.setName("Ritik Jani");
        customer.setEmail("ritik@gmail.com");
        customer.setPhone("9876543210");
        customer.setAadhar("123456789012");
        customer.setAddress("Bhilai");
        customer.setPasswordHash("hashed");
        customer.setStatus(Status.ACTIVE);

        entityManager.persist(customer);

        account = new Account();
        account.setAccountNum(111122223333L);
        account.setAmount(new BigDecimal("10000"));
        account.setAccountType(AccountType.Saving);
        account.setAccountStatus(Status.ACTIVE);
        account.setPinHash("1234");
        account.setBankId(1L);
        account.setCustomer(customer);

        entityManager.persist(account);
        entityManager.flush();
    }

    @Test
    void shouldFindAccountByAccountNumAndCustomerId() {

        // Act
        Optional<Account> result = accountRepository.findByAccountNumAndCustomer_CustomerId(
                        111122223333L,
                        customer.getCustomerId());

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(111122223333L, result.get().getAccountNum());
        Assertions.assertEquals(customer.getCustomerId(), result.get().getCustomer().getCustomerId());
    }

    @Test
    void shouldReturnEmptyWhenAccountNumDoesNotMatch() {

        Optional<Account> result = accountRepository.findByAccountNumAndCustomer_CustomerId(999999999999L,
                        customer.getCustomerId());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenCustomerIdDoesNotMatch() {

        Optional<Account> result =
                accountRepository.findByAccountNumAndCustomer_CustomerId(111122223333L, UUID.randomUUID());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllAccountsByCustomerId() {

        Account secondAccount = new Account();
        secondAccount.setAccountNum(222222222222L);
        secondAccount.setAmount(new BigDecimal("5000"));
        secondAccount.setAccountType(AccountType.Current);
        secondAccount.setAccountStatus(Status.ACTIVE);
        secondAccount.setPinHash("5678");
        secondAccount.setBankId(1L);
        secondAccount.setCustomer(customer);

        entityManager.persist(secondAccount);
        entityManager.flush();

        // Act
        List<Account> accounts = accountRepository.findByCustomer_CustomerId(customer.getCustomerId());

        // Assert
        Assertions.assertEquals(2, accounts.size());
        Assertions.assertTrue(accounts.stream().allMatch(
                a -> a.getCustomer()
                        .getCustomerId()
                        .equals(customer.getCustomerId()))
        );
    }

    @Test
    void shouldReturnEmptyListWhenCustomerHasNoAccounts() {

        Customer newCustomer = new Customer();
        newCustomer.setName("Another User");
        newCustomer.setEmail("test@gmail.com");
        newCustomer.setPhone("9876543211");
        newCustomer.setAadhar("123456789013");
        newCustomer.setAddress("Delhi");
        newCustomer.setPasswordHash("hashed");
        newCustomer.setStatus(Status.ACTIVE);

        entityManager.persist(newCustomer);
        entityManager.flush();

        List<Account> accounts =
                accountRepository.findByCustomer_CustomerId(
                        newCustomer.getCustomerId()
                );

        Assertions.assertTrue(accounts.isEmpty());
    }
}
