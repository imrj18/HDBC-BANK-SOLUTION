package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.customerDTO.CustomerRegisterDTO;
import com.ritik.customer_microservice.dto.customerDTO.CustomerResponseDTO;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.exception.CustomerAlreadyExistsException;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerRegisterDTO registerDTO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        registerDTO = new CustomerRegisterDTO();
        registerDTO.setName("Ritik Jani");
        registerDTO.setEmail("ritik@gmail.com");
        registerDTO.setPhone("9876543210");
        registerDTO.setAddress("Bhilai, Chhattisgarh");
        registerDTO.setAadhar("123456789012");
        registerDTO.setPassword(
                passwordEncoder.encode("R@123")
        );
    }

    @Test
    void shouldRegisterCustomerSuccessfully() {

        // Arrange
        Mockito.when(customerRepository.existsByEmail(registerDTO.getEmail()))
                .thenReturn(false);
        Mockito.when(customerRepository.existsByAadhar(registerDTO.getAadhar()))
                .thenReturn(false);
        Mockito.when(customerRepository.existsByPhone(registerDTO.getPhone()))
                .thenReturn(false);

        Customer savedCustomer = new Customer();
        savedCustomer.setCustomerId(UUID.randomUUID());
        savedCustomer.setName(registerDTO.getName());
        savedCustomer.setEmail(registerDTO.getEmail());
        savedCustomer.setPhone(registerDTO.getPhone());
        savedCustomer.setAddress(registerDTO.getAddress());
        savedCustomer.setAadhar(registerDTO.getAadhar());
        savedCustomer.setPasswordHash(
                passwordEncoder.encode(registerDTO.getPassword())
        );
        savedCustomer.setStatus(Status.ACTIVE);
        savedCustomer.setCreatedAt(LocalDateTime.now());

        Mockito.when(customerRepository.save(Mockito.any(Customer.class)))
                .thenReturn(savedCustomer);

        // Act
        CustomerResponseDTO response = customerService.register(registerDTO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(registerDTO.getEmail(), response.getEmail());
        Assertions.assertEquals(registerDTO.getPhone(), response.getPhone());

        Mockito.verify(customerRepository).save(Mockito.any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        //Arrange
        Mockito.when(customerRepository.existsByEmail(registerDTO.getEmail()))
                .thenReturn(true);

        //Act + Assert
        CustomerAlreadyExistsException ex =
                Assertions.assertThrows(CustomerAlreadyExistsException.class,
                        () -> customerService.register(registerDTO));

        Assertions.assertEquals("Email already exists", ex.getMessage());

        Mockito.verify(customerRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenAadharAlreadyExists() {

        //Arrange
        Mockito.when(customerRepository.existsByAadhar(registerDTO.getAadhar()))
                .thenReturn(true);

        //Act + Assert
        CustomerAlreadyExistsException ex =
                Assertions.assertThrows(CustomerAlreadyExistsException.class,
                        () -> customerService.register(registerDTO));

        Assertions.assertEquals("Aadhaar already exists", ex.getMessage());

        Mockito.verify(customerRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {

        //Arrange
        Mockito.when(customerRepository.existsByPhone(registerDTO.getPhone()))
                .thenReturn(true);

        //Act + Assert
        CustomerAlreadyExistsException ex =
                Assertions.assertThrows(CustomerAlreadyExistsException.class,
                        () -> customerService.register(registerDTO));

        Assertions.assertEquals("Phone already exists", ex.getMessage());

        Mockito.verify(customerRepository, Mockito.never())
                .save(Mockito.any());
    }
}
