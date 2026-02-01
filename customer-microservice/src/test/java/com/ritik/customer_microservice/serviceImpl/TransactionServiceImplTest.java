package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.transactionDTO.*;
import com.ritik.customer_microservice.enums.OperationType;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.exception.*;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.Transaction;
import com.ritik.customer_microservice.repository.AccountRepository;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private OtpServiceImpl otpService;

    @Mock
    private CacheManager cacheManager;

    private DepositRequestDTO depositRequestDTO;
    private WithdrawRequestDTO withdrawRequestDTO;
    private Customer customer;
    private Account account;
    private Transaction transaction;
    private Account senderAccount;
    private Account receiverAccount;
    private TransferRequestDTO transferRequestDTO;
    private Pageable pageable;
    private ConfirmRequestDTO confirmRequestDTO;

    @BeforeEach
    void setUp(){
        depositRequestDTO = new DepositRequestDTO();
        depositRequestDTO.setAmount(BigDecimal.valueOf(2000));
        depositRequestDTO.setAccountNum(123456789L);

        withdrawRequestDTO = new WithdrawRequestDTO();
        withdrawRequestDTO.setAccountNum(123456789L);
        withdrawRequestDTO.setAmount(BigDecimal.valueOf(2000));
        withdrawRequestDTO.setPin("1234");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setEmail("test@gmail.com");

        account = new Account();
        account.setAccountId(UUID.randomUUID());
        account.setAmount(BigDecimal.ZERO);
        account.setPinHash("pin-hash");

        transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());

        transferRequestDTO = new TransferRequestDTO();
        transferRequestDTO.setAmount(BigDecimal.valueOf(2000));
        transferRequestDTO.setPin("1234");
        transferRequestDTO.setFromAccountNum(123456789L);
        transferRequestDTO.setToAccountNum(987654321L);

        senderAccount = new Account();
        senderAccount.setAccountId(UUID.randomUUID());
        senderAccount.setAmount(BigDecimal.valueOf(5000));
        senderAccount.setPinHash("encoded-pin");

        receiverAccount = new Account();
        receiverAccount.setAccountId(UUID.randomUUID());
        receiverAccount.setAmount(BigDecimal.valueOf(2000));

        pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        confirmRequestDTO = new ConfirmRequestDTO();
        confirmRequestDTO.setTransactionId(transaction.getTransactionId());
        confirmRequestDTO.setOTP("0000");

    }

    @Test
    void shouldDepositMoneySuccessFully(){

        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository
                .findByAccountNumAndCustomer_CustomerId(depositRequestDTO.getAccountNum(), customer.getCustomerId()))
                .thenReturn(Optional.of(account));

        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        TransactionResponseDTO response =
                transactionService.depositMoney("test@gmail.com", depositRequestDTO);

        //Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(depositRequestDTO.getAmount(), response.getAmount());

        Mockito.verify(transactionRepository).save(Mockito.any(Transaction.class));
    }         //---------------------------------------1

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringDepositMoney(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        //Act + Assert
        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                ()->transactionService.depositMoney("test@gmail.com",depositRequestDTO));

        Assertions.assertEquals("Customer not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }    //----------------2

    @Test
    void shouldThrowExceptionWhenAccountNotFoundDuringDepositMoney(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.ofNullable(customer));
        Mockito.when(accountRepository
                .findByAccountNumAndCustomer_CustomerId(depositRequestDTO.getAccountNum(), customer.getCustomerId()))
                .thenReturn(Optional.empty());

        //Act + Assert
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                ()->transactionService.depositMoney("test@gmail.com",depositRequestDTO));

        Assertions.assertEquals("Account not found", ex.getMessage());

        Mockito.verify(customerRepository).findByEmail(Mockito.any());
        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }     //----------------3

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsZeroDuringDepositMoney() {

        // Arrange
        depositRequestDTO.setAmount(BigDecimal.ZERO);

        // Act + Assert
        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                        () -> transactionService.depositMoney("test@gmail.com", depositRequestDTO));

        Assertions.assertEquals("Deposit amount must be greater than zero", ex.getMessage());

        Mockito.verifyNoInteractions(
                customerRepository,
                accountRepository,
                transactionRepository
        );
    }   //-------4

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsNegativeDuringDepositMoney() {

        // Arrange
        depositRequestDTO.setAmount(BigDecimal.valueOf(-500));

        // Act + Assert
        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () -> transactionService.depositMoney("test@gmail.com", depositRequestDTO));

        Assertions.assertEquals("Deposit amount must be greater than zero", ex.getMessage());

        Mockito.verifyNoInteractions(
                customerRepository,
                accountRepository,
                transactionRepository
        );
    } //-----5

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsZeroDuringWithdrawMoney() {

        // Arrange
        withdrawRequestDTO.setAmount(BigDecimal.ZERO);

        // Act + Assert
        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () -> transactionService.withdrawMoney("test@gmail.com", withdrawRequestDTO));

        Assertions.assertEquals("Withdraw amount must be greater than zero", ex.getMessage());

        Mockito.verifyNoInteractions(customerRepository, accountRepository, transactionRepository);
    } //--------6

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsNegativeDuringWithdrawMoney() {

        // Arrange
        withdrawRequestDTO.setAmount(BigDecimal.valueOf(-200));

        // Act + Assert
        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () -> transactionService.withdrawMoney("test@gmail.com", withdrawRequestDTO));

        Assertions.assertEquals("Withdraw amount must be greater than zero", ex.getMessage());

        Mockito.verifyNoInteractions(customerRepository, accountRepository, transactionRepository);
    } //----7

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringWithdrawMoney(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        //Act + Assert
        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                ()->transactionService.withdrawMoney("test@gmail.com",withdrawRequestDTO));

        Assertions.assertEquals("Customer not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }   //----------------8

    @Test
    void shouldThrowExceptionWhenAccountNotFoundDuringWithdrawMoney(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.ofNullable(customer));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(withdrawRequestDTO.getAccountNum(),
                                customer.getCustomerId())).thenReturn(Optional.empty());

        // Act + Assert
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                ()->transactionService.withdrawMoney("test@gmail.com",withdrawRequestDTO));

        Assertions.assertEquals("Account not found", ex.getMessage());

        Mockito.verify(customerRepository).findByEmail(Mockito.any());
        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }  //------------------9

    @Test
    void shouldThrowWrongPinExceptionWhenWrongPinDuringWithdrawMoney() {

        // Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.ofNullable(customer));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(withdrawRequestDTO.getAccountNum(),
                        customer.getCustomerId())).thenReturn(Optional.of(account));

        Mockito.when(passwordEncoder.matches(withdrawRequestDTO.getPin(), account.getPinHash()))
                .thenReturn(false);

        // Act + Assert
        Assertions.assertThrows(WrongPinException.class,
                () -> transactionService.withdrawMoney("test@gmail.com", withdrawRequestDTO));

        Mockito.verify(customerRepository).findByEmail(Mockito.any());
        Mockito.verify(accountRepository).findByAccountNumAndCustomer_CustomerId(Mockito.any(), Mockito.any());
        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    } //----------------10

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenInsufficientAmountDuringWithdrawMoney() {

        // Arrange
        withdrawRequestDTO.setAmount(BigDecimal.valueOf(20000));
        account.setAmount(BigDecimal.valueOf(18000));

        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.ofNullable(customer));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(withdrawRequestDTO.getAccountNum(),
                        customer.getCustomerId())).thenReturn(Optional.of(account));
        Mockito.when(passwordEncoder.matches(withdrawRequestDTO.getPin(), account.getPinHash()))
                .thenReturn(true);

        // Act + Assert
        InsufficientBalanceException ex = Assertions.assertThrows(InsufficientBalanceException.class,
                () -> transactionService.withdrawMoney("test@gmail.com", withdrawRequestDTO));

        Assertions.assertEquals("Insufficient balance", ex.getMessage());

        Mockito.verify(customerRepository).findByEmail(Mockito.any());
        Mockito.verify(accountRepository).findByAccountNumAndCustomer_CustomerId(Mockito.any(), Mockito.any());
        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }  //--11

    @Test
    void shouldCreatePendingWithdrawTransactionSuccessfully() {
        // Arrange
        account.setAmount(BigDecimal.valueOf(2000));
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(withdrawRequestDTO.getAccountNum(),
                        customer.getCustomerId())).thenReturn(Optional.of(account));

        Mockito.when(passwordEncoder.matches(withdrawRequestDTO.getPin(), account.getPinHash()))
                .thenReturn(true);

        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.doNothing().when(otpService).sendOtp(Mockito.anyString(), Mockito.any());

        // Act
        TransactionResponseDTO response = transactionService.withdrawMoney("test@gmail.com", withdrawRequestDTO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(withdrawRequestDTO.getAmount(), response.getAmount());
        Assertions.assertEquals(TransactionStatus.PENDING, response.getTransactionStatus());

        Mockito.verify(transactionRepository).save(Mockito.any(Transaction.class));
        Mockito.verify(otpService).sendOtp(Mockito.eq("test@gmail.com"), Mockito.any());
    }  //----------------------------------------12

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringTransactionHistory(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        //Act + Assert
        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                ()->transactionService.transactionHistory("test@gmail.com",null,0, 5));

        Assertions.assertEquals("Customer not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }  //-----------13

    @Test
    void shouldThrowAccountNotFoundWhenAccountNumProvidedInTransactionHistory() {

        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository
                        .findByAccountNumAndCustomer_CustomerId(123456789L, customer.getCustomerId()))
                .thenReturn(Optional.empty());

        //Act + Assert
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> transactionService.transactionHistory(
                        "test@gmail.com",
                        123456789L,
                        0,
                        5));

        Assertions.assertEquals("Account not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    } //-------14

    @Test
    void shouldThrowTransactionNotFoundForAccountWhenAccountNumProvidedInTransactionHistory() {

        // Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(customer));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(
                                123456789L, customer.getCustomerId())).thenReturn(Optional.of(account));

        Page<Transaction> emptyPage = Page.empty(pageable);

        Mockito.when(transactionRepository.findByAccount_AccountId(account.getAccountId(), pageable))
                .thenReturn(emptyPage);

        // Act + Assert
        TransactionNotFoundException ex =
                Assertions.assertThrows(TransactionNotFoundException.class,
                        () -> transactionService.transactionHistory(
                                "test@gmail.com",
                                123456789L,
                                0,
                                5
                        ));

        Assertions.assertEquals("Transactions not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldGetTransactionHistoryWhenAccountNumProvided() {
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(
                123456789L, customer.getCustomerId())).thenReturn(Optional.of(account));

        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        Mockito.when(transactionRepository.findByAccount_AccountId(account.getAccountId(),pageable))
                .thenReturn(transactionPage);

        //Act
        PageResponse<TransactionHistoryDTO> response =
                transactionService.transactionHistory("test@gmail.com", 123456789L,0,5);

        //Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.getData().size());

        Mockito.verify(transactionRepository).findByAccount_AccountId(account.getAccountId(),pageable);
    }   //------------------------16

    @Test
    void shouldThrowAccountNotFoundWhenAccountNumNotProvidedInTransactionHistory() {
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository.findByCustomer_CustomerId(customer.getCustomerId()))
                .thenReturn(Collections.emptyList());

        //Act + Assert
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> transactionService.transactionHistory("test@gmail.com", null,0,5));

        Assertions.assertEquals("No accounts found for customer", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }  //----17

    @Test
    void shouldThrowTransactionNotFoundWhenAccountNumNotProvidedInTransactionHistory() {

        // Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        account.setAccountId(UUID.randomUUID());

        Mockito.when(accountRepository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(List.of(account));

        Page<Transaction> emptyPage = Page.empty();
        Mockito.when(transactionRepository.findByAccount_AccountIdIn(List.of(account.getAccountId()),pageable))
                .thenReturn(emptyPage);

        // Act + Assert
        TransactionNotFoundException ex = Assertions.assertThrows(TransactionNotFoundException.class,
                () -> transactionService.transactionHistory("test@gmail.com", null,0,5));

        Assertions.assertEquals("Transactions not found", ex.getMessage());

        Mockito.verify(customerRepository).findByEmail("test@gmail.com");

        Mockito.verify(accountRepository).findByCustomer_CustomerId(customer.getCustomerId());

        Mockito.verify(transactionRepository).findByAccount_AccountIdIn(List.of(account.getAccountId()),pageable);

        Mockito.verify(accountRepository, Mockito.never())
                .findByAccountNumAndCustomer_CustomerId(Mockito.any(), Mockito.any());
    }  //-----18

    @Test
    void shouldGetTransactionHistoryWhenAccountNumNotProvidedInTransactionHistory() {

        // Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        account.setAccountId(UUID.randomUUID());

        Mockito.when(accountRepository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(List.of(account));

        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        Mockito.when(transactionRepository.findByAccount_AccountIdIn(List.of(account.getAccountId()),pageable))
                .thenReturn(transactionPage);

        // Act
        PageResponse<TransactionHistoryDTO> response = transactionService
                .transactionHistory("test@gmail.com", null,0,5);

        //Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.getData().size());

        // Verify correct interactions
        Mockito.verify(customerRepository).findByEmail("test@gmail.com");

        Mockito.verify(accountRepository).findByCustomer_CustomerId(customer.getCustomerId());

        Mockito.verify(transactionRepository).findByAccount_AccountIdIn(List.of(account.getAccountId()),pageable);

        Mockito.verify(accountRepository, Mockito.never())
                .findByAccountNumAndCustomer_CustomerId(Mockito.any(), Mockito.any());
    }  //---19

    @Test
    void shouldCreatePendingTransferMoneyTransactionSuccessfully() {

        // Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository.findByAccountNum(
                        transferRequestDTO.getToAccountNum()))
                .thenReturn(Optional.of(receiverAccount));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(
                        transferRequestDTO.getFromAccountNum(), customer.getCustomerId()))
                .thenReturn(Optional.of(senderAccount));

        Mockito.when(passwordEncoder.matches(transferRequestDTO.getPin(), senderAccount.getPinHash()))
                .thenReturn(true);

        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.doNothing().when(otpService).sendOtp(Mockito.anyString(), Mockito.any());

        // Act
        TransferResponseDTO response = transactionService.transferMoney("test@gmail.com", transferRequestDTO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(TransactionStatus.PENDING, response.getStatus());
        Assertions.assertEquals(transferRequestDTO.getAmount(), response.getAmount());
        Assertions.assertNotNull(response.getTransactionReferenceId());
        Assertions.assertNotEquals(response.getFromAccountNum(), response.getToAccountNum());

        // Verify interactions
        Mockito.verify(accountRepository).findByAccountNumAndCustomer_CustomerId(
                transferRequestDTO.getFromAccountNum(), customer.getCustomerId());

        Mockito.verify(accountRepository).findByAccountNum(
                transferRequestDTO.getToAccountNum());

        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }  //-------------------------------------------20

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsZeroInTransferMoney() {
        // Arrange
        transferRequestDTO.setAmount(BigDecimal.ZERO);

        //Act + Assert
        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () -> transactionService.transferMoney("test@gmail.com", transferRequestDTO));

        Assertions.assertEquals("Transfer amount must be greater than zero", ex.getMessage());

        Mockito.verifyNoInteractions(customerRepository, accountRepository, transactionRepository);
    }  //----------21

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsNegativeInTransferMoney() {
        //Arrange
        transferRequestDTO.setAmount(BigDecimal.valueOf(-500));

        //Act + Assert
        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () -> transactionService.transferMoney("test@gmail.com", transferRequestDTO));

        Assertions.assertEquals("Transfer amount must be greater than zero", ex.getMessage());

        Mockito.verifyNoInteractions(customerRepository, accountRepository, transactionRepository);
    }  //------22

    @Test
    void shouldThrowExceptionWhenSenderAndReceiverSame() {
        //Arrange
        transferRequestDTO.setFromAccountNum(123456L);
        transferRequestDTO.setToAccountNum(123456L);

        //Act + Assert
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney("test@gmail.com", transferRequestDTO));
        Assertions.assertEquals("Sender and receiver accounts cannot be same", ex.getMessage());
    }  //-----------------------------23

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringTransferMoney(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        //Act + Assert
        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                ()->transactionService.transferMoney("test@gmail.com",transferRequestDTO));

        Assertions.assertEquals("Customer not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }  //----------------24

    @Test
    void shouldThrowExceptionWhenSenderAccountNotFoundDuringTransferMoney(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.ofNullable(customer));
        Mockito.when(accountRepository.findByAccountNum(
                        transferRequestDTO.getToAccountNum()))
                .thenReturn(Optional.of(receiverAccount));
        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(
                transferRequestDTO.getFromAccountNum(),customer.getCustomerId()))
                .thenReturn(Optional.empty());

        //Act + Assert
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                ()->transactionService.transferMoney("test@gmail.com",transferRequestDTO));

        Assertions.assertEquals("Account not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    } //------------25

    @Test
    void shouldThrowExceptionWhenReceiverAccountNotFoundDuringTransferMoney(){
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.ofNullable(customer));
        Mockito.when(accountRepository.findByAccountNum(
                        transferRequestDTO.getToAccountNum()))
                .thenReturn(Optional.empty());


        //Act + Assert
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                ()->transactionService.transferMoney("test@gmail.com",transferRequestDTO));

        Assertions.assertEquals("Receiver account not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }  //---------26

    @Test
    void shouldThrowWrongPinException() {
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(
                transferRequestDTO.getFromAccountNum(), customer.getCustomerId()))
                .thenReturn(Optional.of(senderAccount));

        Mockito.when(accountRepository.findByAccountNum(
                transferRequestDTO.getToAccountNum()))
                .thenReturn(Optional.of(receiverAccount));

        Mockito.when(passwordEncoder.matches(
                        transferRequestDTO.getPin(), senderAccount.getPinHash())).thenReturn(false);

        //Act  + Assert
        WrongPinException ex = Assertions.assertThrows(WrongPinException.class,
                () -> transactionService.transferMoney("test@gmail.com", transferRequestDTO));

        Assertions.assertEquals("Wrong PIN", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }  //----------------------------------------------27

    @Test
    void shouldThrowInsufficientBalanceException() {
        //Arrange
        Mockito.when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(customer));

        senderAccount.setAmount(BigDecimal.valueOf(1000));
        senderAccount.setPinHash("pin-hash");

        transferRequestDTO.setAmount(BigDecimal.valueOf(5000));

        Mockito.when(accountRepository.findByAccountNumAndCustomer_CustomerId(
                        transferRequestDTO.getFromAccountNum(), customer.getCustomerId()))
                .thenReturn(Optional.of(senderAccount));

        Mockito.when(accountRepository.findByAccountNum(
                        transferRequestDTO.getToAccountNum()))
                .thenReturn(Optional.of(receiverAccount));

        Mockito.when(passwordEncoder.matches(Mockito.any(), Mockito.any())).thenReturn(true);

        //Act + Assert
        InsufficientBalanceException ex = Assertions.assertThrows(InsufficientBalanceException.class,
                () -> transactionService.transferMoney("test@gmail.com", transferRequestDTO));

        Assertions.assertEquals("Insufficient balance", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }  //-----------------------------------28

    @Test
    void shouldConfirmWithdrawTransactionSuccessfully() {
        // Arrange
        senderAccount.setCustomer(customer);

        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setAccount(senderAccount);
        transaction.setAmount(BigDecimal.valueOf(2000));
        transaction.setOperationType(OperationType.WITHDRAW);

        Mockito.when(transactionRepository.findByTransactionId(confirmRequestDTO.getTransactionId()))
                .thenReturn(Optional.of(transaction));

        Mockito.when(otpService.verifyOtp(customer.getEmail(), transaction.getTransactionId(),
                confirmRequestDTO.getOTP())).thenReturn(true);

        Mockito.when(accountRepository.save(Mockito.any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TransactionResponseDTO response =
                transactionService.transactionConfirm(customer.getEmail(), confirmRequestDTO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(TransactionStatus.SUCCESS, response.getTransactionStatus());
        Assertions.assertEquals(BigDecimal.valueOf(3000), senderAccount.getAmount());

        Mockito.verify(accountRepository).save(senderAccount);
        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        Mockito.when(transactionRepository.findByTransactionId(confirmRequestDTO.getTransactionId()))
                .thenReturn(Optional.empty());

        TransactionNotFoundException ex =
                Assertions.assertThrows(TransactionNotFoundException.class,
                        () -> transactionService.transactionConfirm(customer.getEmail(), confirmRequestDTO));

        Assertions.assertEquals("Transaction not found", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenTransactionAlreadyProcessed() {
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        Mockito.when(transactionRepository.findByTransactionId(confirmRequestDTO.getTransactionId()))
                .thenReturn(Optional.of(transaction));

        TransactionAlreadyProcessedException ex = Assertions.assertThrows(TransactionAlreadyProcessedException.class,
                        () -> transactionService.transactionConfirm(customer.getEmail(), confirmRequestDTO));

        Assertions.assertEquals("Transaction already processed", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserUnauthorized() {
        // Arrange
        customer.setEmail("owner@gmail.com");

        senderAccount.setCustomer(customer);

        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setAccount(senderAccount);
        transaction.setAmount(BigDecimal.valueOf(2000));

        Mockito.when(transactionRepository.findByTransactionId(confirmRequestDTO.getTransactionId()))
                .thenReturn(Optional.of(transaction));

        // Act + Assert
        UnauthorizedException ex = Assertions.assertThrows(
                UnauthorizedException.class,
                () -> transactionService.transactionConfirm("hacker@gmail.com", confirmRequestDTO)
        );

        Assertions.assertEquals("Unauthorized", ex.getMessage());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldConfirmTransferTransactionSuccessfully() {
        // Arrange
        senderAccount.setCustomer(customer);
        senderAccount.setAmount(BigDecimal.valueOf(5000));

        transaction.setAccount(senderAccount);
        transaction.setAmount(BigDecimal.valueOf(2000)); // 🔥 REQUIRED
        transaction.setOperationType(OperationType.TRANSFER);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setCounterpartyAccountNum(987654L);

        Account receiver = new Account();
        receiver.setAmount(BigDecimal.valueOf(1000));

        Mockito.when(transactionRepository.findByTransactionId(confirmRequestDTO.getTransactionId()))
                .thenReturn(Optional.of(transaction));

        Mockito.when(otpService.verifyOtp(
                customer.getEmail(),
                confirmRequestDTO.getTransactionId(),
                confirmRequestDTO.getOTP()
        )).thenReturn(true);

        Mockito.when(accountRepository.findByAccountNum(987654L)).thenReturn(Optional.of(receiver));

        Mockito.when(accountRepository.save(Mockito.any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TransactionResponseDTO response = transactionService.transactionConfirm(customer.getEmail(), confirmRequestDTO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(TransactionStatus.SUCCESS, response.getTransactionStatus());
        Assertions.assertEquals(BigDecimal.valueOf(3000), senderAccount.getAmount());

        Mockito.verify(transactionRepository, Mockito.times(2))
                .save(Mockito.any(Transaction.class));
    }

}
