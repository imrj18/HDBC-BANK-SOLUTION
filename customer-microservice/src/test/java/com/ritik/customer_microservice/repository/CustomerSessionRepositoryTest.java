package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.CustomerSession;
import com.ritik.customer_microservice.repository.CustomerSessionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class CustomerSessionRepositoryTest {

    @Autowired
    private CustomerSessionRepository customerSessionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CustomerSession session;

    @BeforeEach
    void setup() {

        Customer customer = new Customer();
        customer.setName("Ritik Jani");
        customer.setEmail("rj@example.com");
        customer.setPhone("9876543210");
        customer.setAadhar("123456789012");
        customer.setAddress("Bhilai");
        customer.setPasswordHash("hashed");
        customer.setStatus(Status.ACTIVE);

        entityManager.persist(customer);
        entityManager.flush();

        session = new CustomerSession();
        session.setCustomer(customer);
        session.setToken("jwt-token-123");
        session.setExpiryTime(LocalDateTime.now().plusMinutes(30));
        session.setLastActivityTime(LocalDateTime.now());

        entityManager.persist(session);
        entityManager.flush();
    }

    @Test
    void shouldFindCustomerSessionByToken() {

        Optional<CustomerSession> result =
                customerSessionRepository.findByToken("jwt-token-123");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("jwt-token-123", result.get().getToken());
    }

    @Test
    void shouldReturnEmptyWhenTokenNotFound() {

        Optional<CustomerSession> result =
                customerSessionRepository.findByToken("invalid-token");

        Assertions.assertTrue(result.isEmpty());
    }
}


