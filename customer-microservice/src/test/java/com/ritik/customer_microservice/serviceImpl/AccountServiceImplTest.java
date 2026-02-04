package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.accountDTO.AccountBalanceDTO;
import com.ritik.customer_microservice.dto.accountDTO.AccountResponseDTO;
import com.ritik.customer_microservice.dto.accountDTO.CreateAccountDTO;
import com.ritik.customer_microservice.dto.external.BankResponseDTO;
import com.ritik.customer_microservice.enums.AccountType;
import com.ritik.customer_microservice.exception.AccountNotFoundException;
import com.ritik.customer_microservice.exception.BankNotFoundException;
import com.ritik.customer_microservice.exception.CustomerNotFoundException;
import com.ritik.customer_microservice.exception.ServiceUnavailableException;
import com.ritik.customer_microservice.feign.BankClient;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.AccountRepository;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.wrapper.PageResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BankClient bankClient;

    @Mock
    private GenerateAccountNumber accountNumberGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private AccountServiceImpl accountService;

    private CreateAccountDTO createAccountDTO;
    private Customer customer;
    private Account account;

    @BeforeEach
    void SetUp(){
        createAccountDTO = new CreateAccountDTO();
        createAccountDTO.setAccountType(AccountType.Saving);
        createAccountDTO.setIfscCode("HDBC0000001");
        createAccountDTO.setPin("1234");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setEmail("test@example.com");

        account = new Account();
        account.setCustomer(customer);
        account.setAmount(BigDecimal.ZERO);
        account.setAccountNum(1234567890L);

    }

    @Test
    void shouldThrowExceptionWhenBankNotFoundDuringCreateAccount() {
        //Arrange
        Mockito.when(bankClient.getBanks(Mockito.anyString(), Mockito.isNull())).thenReturn(new PageResponse<>(
                Collections.emptyList(),
                0,
                1,
                5,
                true));

        //Act + Assert
        BankNotFoundException ex = Assertions.assertThrows(BankNotFoundException.class,
                        () -> accountService.createAccount("test@example.com", createAccountDTO));

        Assertions.assertEquals("Invalid IFSC code", ex.getMessage());

        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());

        Mockito.verify(customerRepository, Mockito.never()).findByEmail(Mockito.anyString());
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        //Arrange
        BankResponseDTO bank = new BankResponseDTO();
        bank.setBankId(1L);

        Mockito.when(bankClient.getBanks(Mockito.anyString(), Mockito.isNull())).thenReturn(new PageResponse<>(
                List.of(bank),0,1,5,true
        ));

        Mockito.when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountNumberGenerator.generate(1L)).thenReturn(1234567890L);

        Mockito.when(accountRepository.save(Mockito.any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(passwordEncoder.encode(Mockito.any()))
                .thenReturn("encoded-pin");

        //Act
        AccountResponseDTO response = accountService.createAccount("test@example.com", createAccountDTO);

        //Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getBankId());
        Assertions.assertEquals(1234567890L, response.getAccountNum());

        Mockito.verify(accountRepository).save(Mockito.any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenFeignNotFoundOccurs() {
        //Arrange
        Mockito.when(bankClient.getBanks(Mockito.anyString(), Mockito.isNull()))
                .thenThrow(Mockito.mock(feign.FeignException.NotFound.class));

        //Act + Assert
        BankNotFoundException ex = Assertions.assertThrows(BankNotFoundException.class,
                () -> accountService.createAccount("test@example.com", createAccountDTO));

        Assertions.assertEquals("Invalid IFSC code", ex.getMessage());
    }

    @Test
    void shouldThrowServiceUnavailableExceptionWhenFeignFails() {
        //Arrange
        Mockito.when(bankClient.getBanks(Mockito.anyString(), Mockito.isNull()))
                .thenThrow(Mockito.mock(feign.FeignException.class));

        //Act + Assert
        ServiceUnavailableException ex = Assertions.assertThrows(ServiceUnavailableException.class,
                () -> accountService.createAccount("test@example.com", createAccountDTO));

        Assertions.assertTrue(ex.getMessage().contains("Bank service is currently unavailable"));
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        //Arrange
        BankResponseDTO bank = new BankResponseDTO();
        bank.setBankId(1L);

        Mockito.when(bankClient.getBanks(Mockito.anyString(), Mockito.isNull())).thenReturn(new PageResponse<>(
                List.of(bank),0,1,5,true
        ));

        Mockito.when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        //Act + Assert
        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                        () -> accountService.createAccount("test@example.com", createAccountDTO));

        Assertions.assertEquals("Customer not found", ex.getMessage());

        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldCheckBalanceSuccessfully() {
        // Arrange
        String email = "test@gmail.com";

        Mockito.when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(
                1234567890L, customer.getCustomerId())).thenReturn(Optional.of(account));

        // Act
        AccountBalanceDTO response = accountService.checkBalance(email, 1234567890L);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(BigDecimal.ZERO, response.getAccountBalance());
        Assertions.assertEquals(1234567890L, response.getAccountNumber());

        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringCheckBalance() {

        Mockito.when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                () -> accountService.checkBalance("test@example.com", account.getAccountNum()));

        Assertions.assertEquals("Customer not found", ex.getMessage());

        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundDuringCheckBalance() {

        Mockito.when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));
        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(
                account.getAccountNum(),customer.getCustomerId())).thenReturn(Optional.empty());

        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> accountService.checkBalance("test@example.com", account.getAccountNum()));

        Assertions.assertEquals("Account not found", ex.getMessage());

        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldGetAccountInfoSuccessfullyWithoutAccountNum() {
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));

        account.setAccountId(UUID.randomUUID());
        account.setAccountNum(1234567890L);

        List<Account> accounts = List.of(account);

        Mockito.when(accountRepository
                        .findByCustomer_CustomerId(customer.getCustomerId()))
                .thenReturn(accounts);

        // Act
        PageResponse<AccountResponseDTO> response = accountService.getAccountInfo("test@example.com", null);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.getTotalItems());
        Assertions.assertEquals(account.getAccountNum(), response.getData().get(0).getAccountNum());

        Mockito.verify(customerRepository).findByEmail("test@example.com");
        Mockito.verify(accountRepository)
                .findByCustomer_CustomerId(customer.getCustomerId());
    }


    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringAccountInfo(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                () -> accountService.getAccountInfo("test@example.com", account.getAccountNum()));

        //Act + Assert
        Assertions.assertEquals("Customer not found", ex.getMessage());

        Mockito.verify(accountRepository, Mockito.never()).findByCustomer_CustomerId(Mockito.any());

        Mockito.verify(accountRepository, Mockito.never())
                .findByAccountNumAndCustomer_CustomerId(Mockito.any(), Mockito.any());

    }

    @Test
    void shouldGetAccountInfoSuccessfullyWithAccountNum() {

        //Arrange
        Mockito.when(customerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(customer));

        account.setAccountId(UUID.randomUUID());
        account.setAccountNum(1234567890L);

        Mockito.when(accountRepository
                        .findByAccountNumAndCustomer_CustomerId(account.getAccountNum(),customer.getCustomerId()))
                .thenReturn(Optional.of(account));

        // Act
        PageResponse<AccountResponseDTO> response = accountService
                .getAccountInfo("test@example.com", 1234567890L);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.getTotalPages());
        Assertions.assertEquals(account.getAccountNum(), response.getData().get(0).getAccountNum());

        Mockito.verify(customerRepository).findByEmail("test@example.com");
        Mockito.verify(accountRepository)
                .findByAccountNumAndCustomer_CustomerId(account.getAccountNum(),customer.getCustomerId());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundDuringGetAccountInfoWithoutAccountNum() {
        // Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository.findByCustomer_CustomerId(customer.getCustomerId()))
                .thenReturn(Collections.emptyList());

        // Act + Assert
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
            () -> accountService.getAccountInfo("test@gmail.com", null));

        Assertions.assertEquals("Account not found.", ex.getMessage());

        Mockito.verify(accountRepository).findByCustomer_CustomerId(customer.getCustomerId());

        Mockito.verify(accountRepository, Mockito.never())
                .findByAccountNumAndCustomer_CustomerId(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundDuringGetAccountInfoWithAccountNum() {
        // Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository
                        .findByAccountNumAndCustomer_CustomerId(1234567890L,customer.getCustomerId()))
                .thenReturn(Optional.empty());

        // Act + Assert
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccountInfo("test@gmail.com", 1234567890L));

        Assertions.assertEquals("Account not found", ex.getMessage());

        Mockito.verify(accountRepository, Mockito.never()).findByCustomer_CustomerId(customer.getCustomerId());

        Mockito.verify(accountRepository).findByAccountNumAndCustomer_CustomerId(Mockito.any(), Mockito.any());
    }

}
