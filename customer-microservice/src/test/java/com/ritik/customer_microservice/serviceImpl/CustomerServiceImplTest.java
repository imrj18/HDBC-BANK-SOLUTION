package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.customerDTO.*;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.exception.AlreadyLoggedInException;
import com.ritik.customer_microservice.exception.CustomerAlreadyExistsException;
import com.ritik.customer_microservice.exception.CustomerNotFoundException;
import com.ritik.customer_microservice.exception.WrongPasswordException;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.CustomerSession;
import com.ritik.customer_microservice.model.Transaction;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.repository.CustomerSessionRepository;
import com.ritik.customer_microservice.serviceImpl.CustomerServiceImpl;
import com.ritik.customer_microservice.serviceImpl.JwtServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerSessionRepository customerSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtServiceImpl jwtService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerRegisterDTO registerDTO;
    private CustomerLoginDTO loginDTO;
    private CustomerUpdateDTO updateDTO;
    private Customer savedCustomer;
    private Pageable pageable;

    @BeforeEach
    void setUp() {

        registerDTO = new CustomerRegisterDTO();
        registerDTO.setName("Ritik Jani");
        registerDTO.setEmail("ritik@gmail.com");
        registerDTO.setPhone("9876543210");
        registerDTO.setAddress("Bhilai, Chhattisgarh");
        registerDTO.setAadhar("123456789012");
        registerDTO.setPassword("R@123");

        loginDTO = new CustomerLoginDTO();
        loginDTO.setEmail("ritik@gmail.com");
        loginDTO.setPassword("R@1234");

        updateDTO = new CustomerUpdateDTO();
        updateDTO.setName("Rahul Dravid");
        updateDTO.setPhone("9876543211");
        updateDTO.setAddress("Durg, Chhattisgarh");

        savedCustomer = new Customer();
        savedCustomer.setCustomerId(UUID.randomUUID());
        savedCustomer.setName(registerDTO.getName());
        savedCustomer.setEmail(registerDTO.getEmail());
        savedCustomer.setPhone(registerDTO.getPhone());
        savedCustomer.setAddress(registerDTO.getAddress());
        savedCustomer.setAadhar(registerDTO.getAadhar());
        savedCustomer.setPasswordHash("encoded-password");
        savedCustomer.setStatus(Status.ACTIVE);

        pageable = PageRequest.of(0, 5);


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

        Mockito.when(passwordEncoder.encode(Mockito.any()))
                .thenReturn("encoded-password");

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
        Mockito.when(customerRepository.existsByEmail(registerDTO.getEmail())).thenReturn(true);

        //Act + Assert
        CustomerAlreadyExistsException ex = Assertions.assertThrows(CustomerAlreadyExistsException.class,
                        () -> customerService.register(registerDTO));

        Assertions.assertEquals("Email already exists", ex.getMessage());

        Mockito.verify(customerRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenAadharAlreadyExists() {

        //Arrange
        Mockito.when(customerRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        Mockito.when(customerRepository.existsByAadhar(registerDTO.getAadhar())).thenReturn(true);

        //Act + Assert
        CustomerAlreadyExistsException ex = Assertions.assertThrows(CustomerAlreadyExistsException.class,
                        () -> customerService.register(registerDTO));

        Assertions.assertEquals("Aadhaar already exists", ex.getMessage());

        Mockito.verify(customerRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {

        //Arrange
        Mockito.when(customerRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        Mockito.when(customerRepository.existsByAadhar(registerDTO.getAadhar())).thenReturn(false);
        Mockito.when(customerRepository.existsByPhone(registerDTO.getPhone())).thenReturn(true);

        //Act + Assert
        CustomerAlreadyExistsException ex = Assertions.assertThrows(CustomerAlreadyExistsException.class,
                        () -> customerService.register(registerDTO));

        Assertions.assertEquals("Phone already exists", ex.getMessage());

        Mockito.verify(customerRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundInLogin(){

        // Arrange
        Mockito.when(customerRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        //Act+Assert
        Assertions.assertThrows(CustomerNotFoundException.class, () -> customerService.verify(loginDTO));

        Mockito.verify(customerRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenWrongPasswordInLogin(){

        //Arrange
        Mockito.when(customerRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(savedCustomer));

        Mockito.when(passwordEncoder.matches(loginDTO.getPassword(), savedCustomer.getPasswordHash()))
                .thenReturn(false);

        //Act + Assert
        Assertions.assertThrows(WrongPasswordException.class, () -> customerService.verify(loginDTO));

        Mockito.verify(customerRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionCustomerAlreadyLoggedInWhenLogin(){
        //Arrange
        Mockito.when(customerRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(savedCustomer));

        Mockito.when(passwordEncoder.matches(loginDTO.getPassword(), savedCustomer.getPasswordHash()))
                .thenReturn(true);

        CustomerSession activeSession = new CustomerSession();
        activeSession.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        activeSession.setToken("existing-token");

        Mockito.when(customerSessionRepository.findById(savedCustomer.getCustomerId()))
                .thenReturn(Optional.of(activeSession));

        //Act + Assert
        AlreadyLoggedInException ex = Assertions.assertThrows(AlreadyLoggedInException.class,
                () -> customerService.verify(loginDTO));

        Assertions.assertTrue(ex.getMessage().contains("existing-token"));

        Mockito.verify(customerSessionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldLoginSuccessfullyAndReturnTokenInLogin() {
        //Arrange
        Mockito.when(customerRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(savedCustomer));

        Mockito.when(passwordEncoder.matches(loginDTO.getPassword(), savedCustomer.getPasswordHash()))
                .thenReturn(true);

        Mockito.when(customerSessionRepository.findById(savedCustomer.getCustomerId())).thenReturn(Optional.empty());

        String token = "jwt-token";
        Date expiryDate = new Date(System.currentTimeMillis() + 60000);

        Mockito.when(jwtService.generateUserToken(loginDTO.getEmail())).thenReturn(token);

        Mockito.when(jwtService.extractExpiryTime(token)).thenReturn(expiryDate);

        //Act
        String result = customerService.verify(loginDTO);

        //Assert
        Assertions.assertEquals(token, result);

        Mockito.verify(customerSessionRepository).save(Mockito.any(CustomerSession.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundUpdateDetails(){
        //Arrange
        String email = "ritik@example.com";
        Mockito.when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        //Act+Assert
        Assertions.assertThrows(CustomerNotFoundException.class, () -> customerService.updateProfile(email,updateDTO));

        Mockito.verify(customerRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldUpdateProfileSuccessfully() {

        //Arrange
        String email = "ritik@example.com";
        Mockito.when(customerRepository.findByEmail(email)).thenReturn(Optional.of(savedCustomer));

        Mockito.when(customerRepository.save(Mockito.any(Customer.class))).thenReturn(savedCustomer);

        //Act
        CustomerResponseDTO response = customerService.updateProfile(email, updateDTO);

        //Assert
        Assertions.assertEquals(updateDTO.getName(), response.getName());
        Assertions.assertEquals(updateDTO.getPhone(), response.getPhone());
        Assertions.assertEquals(updateDTO.getAddress(), response.getAddress());

        Mockito.verify(customerRepository).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundFetchCustomers() {

        // Arrange
        Long bankId = 1L;
        BigDecimal minBalance = BigDecimal.valueOf(5000);
        BigDecimal maxBalance = BigDecimal.valueOf(8000);


        Mockito.when(customerRepository.findCustomersByBankIdAndBalance(
                Mockito.eq(bankId),
                Mockito.eq(minBalance),
                Mockito.eq(maxBalance),
                Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act + Assert
        Assertions.assertThrows(CustomerNotFoundException.class, () ->
                customerService.fetchCustomersByBankIdAndBalance(bankId, minBalance, maxBalance, 0, 5)
        );

        // Verify
        Mockito.verify(customerRepository).findCustomersByBankIdAndBalance(Mockito.eq(bankId),
                Mockito.eq(minBalance),
                Mockito.eq(maxBalance),
                Mockito.any(Pageable.class));
    }


    @Test
    void shouldFetchCustomersSuccessfully(){
        //Arrange
        Long bankId = 1L;
        BigDecimal minBalance = new BigDecimal(5000);
        BigDecimal maxBalance = new BigDecimal(8000);

        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setName("Ritik Jani");
        customer.setEmail("ritik@gmail.com");

        BigDecimal balance = BigDecimal.valueOf(2500);

        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{customer, balance});

        Page<Object[]> pageResult = new PageImpl<>(results, pageable, results.size());

        Mockito.when(customerRepository.findCustomersByBankIdAndBalance(
                        Mockito.eq(bankId),
                        Mockito.eq(minBalance),
                        Mockito.eq(maxBalance),
                        Mockito.any(Pageable.class)
                ))
                .thenReturn(pageResult);
        //Act
        PageResponse<CustomerBalanceDTO> response = customerService
                .fetchCustomersByBankIdAndBalance(bankId,minBalance,maxBalance,0,5);

        //Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1,response.getData().size());

        CustomerBalanceDTO dto = response.getData().get(0);

        Assertions.assertEquals(customer.getCustomerId(),dto.getCustomerId());
        Assertions.assertEquals(customer.getName(),dto.getName());
        Assertions.assertEquals(customer.getEmail(),dto.getEmail());
        Assertions.assertEquals(balance, dto.getBalance());

        Mockito.verify(customerRepository).findCustomersByBankIdAndBalance(Mockito.eq(bankId),
                Mockito.eq(minBalance),
                Mockito.eq(maxBalance),
                Mockito.any(Pageable.class));
    }
}
