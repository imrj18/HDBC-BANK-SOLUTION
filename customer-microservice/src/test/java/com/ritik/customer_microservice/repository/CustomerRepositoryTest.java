package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.enums.AccountType;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import java.util.Optional;



@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer customer;
    private Account account;

    @Mock
    private Pageable pageable;

    @BeforeEach
    void setup(){
        customer = new Customer();
        customer.setEmail("rj@example.com");
        customer.setName("Ritik Jani");
        customer.setAadhar("123456789012");
        customer.setPhone("9876543210");
        customer.setAddress("Bhilai");
        customer.setPasswordHash("hashed-password");
        customer.setStatus(Status.ACTIVE);

        account = new Account();
        account.setAmount(new BigDecimal("5000"));
        account.setAccountNum(1234567891L);
        account.setPinHash("1234");
        account.setAccountType(AccountType.Saving);
        account.setAccountStatus(Status.ACTIVE);
        account.setBankId(1L);
        account.setCustomer(customer);

        pageable = PageRequest.of(0, 5);
    }

    @Test
    void shouldFindCustomerByEmailExists() {

        //Arrange
        entityManager.persist(customer);
        entityManager.flush();

        // Act
        Optional<Customer> result = customerRepository.findByEmail("rj@example.com");

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Ritik Jani", result.get().getName());
        Assertions.assertEquals("rj@example.com", result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {

        // Act
        Optional<Customer> result = customerRepository.findByEmail("abc@gmail.com");

        // Assert
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenPhoneExists(){

        //Arrange
        entityManager.persist(customer);
        entityManager.flush();

        //Act
        boolean result = customerRepository.existsByPhone("9876543210");

        //Assert
        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenPhoneNotExists(){

        //Arrange
        entityManager.persist(customer);
        entityManager.flush();

        //Act
        boolean result = customerRepository.existsByPhone("9876543211");

        //Assert
        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenAadharExists(){

        //Arrange
        entityManager.persist(customer);
        entityManager.flush();

        //Act
        boolean result = customerRepository.existsByAadhar("123456789012");

        //Assert
        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenAadharNotExists(){

        //Arrange
        entityManager.persist(customer);
        entityManager.flush();

        //Act
        boolean result = customerRepository.existsByAadhar("123456789013");

        //Assert
        Assertions.assertFalse(result);
    }
    @Test
    void shouldReturnTrueWhenEmailExists(){

        //Arrange
        entityManager.persist(customer);
        entityManager.flush();

        //Act
        boolean result = customerRepository.existsByEmail("rj@example.com");

        //Assert
        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenEmailNotExists(){

        //Arrange
        entityManager.persist(customer);
        entityManager.flush();

        //Act
        boolean result = customerRepository.existsByEmail("rj2@example.com");

        //Assert
        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnCustomersByBankIdAndBalanceRange() {

        // Arrange
        entityManager.persist(customer);

        entityManager.persist(account);
        entityManager.flush();

        // Act
        Page<Object[]> result = customerRepository.findCustomersByBankIdAndBalance(
                1L,
                new BigDecimal("3000"),
                new BigDecimal("6000"),
                pageable
        );

        // Assert
        Assertions.assertFalse(result.isEmpty());

        Object[] row = result.getContent().get(0);

        Customer fetchedCustomer = (Customer) row[0];
        BigDecimal balance = (BigDecimal) row[1];

        Assertions.assertEquals("Ritik Jani", fetchedCustomer.getName());
        Assertions.assertEquals(0, balance.compareTo(new BigDecimal("5000")));
    }


    @Test
    void shouldReturnCustomersWhenMinAndMaxBalanceIsNull() {
        entityManager.persist(customer);

        entityManager.persist(account);
        entityManager.flush();

        Page<Object[]> result = customerRepository.findCustomersByBankIdAndBalance(
                        1L,
                        null,
                        null, pageable
                );

        Assertions.assertFalse(result.getContent().isEmpty());
    }

}
