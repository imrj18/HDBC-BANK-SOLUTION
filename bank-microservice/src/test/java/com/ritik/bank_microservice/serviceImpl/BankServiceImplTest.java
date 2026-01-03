package com.ritik.bank_microservice.serviceImpl;


import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import com.ritik.bank_microservice.exception.*;
import com.ritik.bank_microservice.feign.CustomerClient;
import com.ritik.bank_microservice.model.Bank;
import com.ritik.bank_microservice.repository.BankRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class BankServiceImplTest {

    @Mock
    private BankRepository repository;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private BankServiceImpl service;

    private Bank savedEntity;
    private BankRequestDTO dto;
    private CustomerBalanceDTO customerDTO;

    @BeforeEach
    void setUp(){
        savedEntity = new Bank(1L,"HDBC Bank","HDBC0000001",
                "Bhilai",LocalDateTime.now(),LocalDateTime.now());

        dto = new BankRequestDTO();
        dto.setBankName("HDBC Bank");
        dto.setIfscCode("HDBC0000001");
        dto.setBranch("Bhilai");

        customerDTO = new CustomerBalanceDTO(
                UUID.randomUUID(),
                "Ritik",
                "ritik@gmail.com",
                BigDecimal.valueOf(5000)
        );
    }

    @Test
    void shouldRegisterBankInDBSuccessfully(){

        //Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.empty());

        Mockito.when(repository.save(Mockito.any(Bank.class))).thenReturn(savedEntity);

        // Act
        BankResponseDTO actual = service.addBank(dto);


        //Assert
        Assertions.assertEquals(1L,actual.getBankId());
        Assertions.assertEquals("HDBC Bank", actual.getBankName());
        Assertions.assertEquals("HDBC0000001", actual.getIfscCode());
        Assertions.assertEquals("Bhilai", actual.getBranch());

        Mockito.verify(repository, Mockito.times(1))
                .save(Mockito.any(Bank.class));
        Mockito.verify(repository, Mockito.times(1))
                .findByIfscCode("HDBC0000001");
    }

    @Test
    void shouldThrowExceptionWhenIfscCodeAlreadyExists(){

        //Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000001"))
                .thenReturn(Optional.of(savedEntity));

        //Act & Assert
        IfscCodeAlreadyExistException exception = Assertions.assertThrows(IfscCodeAlreadyExistException.class,
                ()->service.addBank(dto));

        Assertions.assertEquals("IFSC code already exists",exception.getMessage());

        Mockito.verify(repository,Mockito.never())
                .save(Mockito.any());
    }




    @Test
    void shouldReturnAllBankDetailsFromDBSuccessfully(){

        //Arrange
        Mockito.when(repository.findAll()).thenReturn(List.of(savedEntity));

        List<BankResponseDTO> actual = service.getBankDetails(null,null);

        //Assert
        Assertions.assertEquals("HDBC Bank", actual.get(0).getBankName());
        Assertions.assertEquals("HDBC0000001",actual.get(0).getIfscCode());
        Assertions.assertEquals("Bhilai", actual.get(0).getBranch());

        Mockito.verify(repository, Mockito.times(1)).
                findAll();
    }

    @Test
    void shouldReturnBankDetailsByIfscCodeFromDBSuccessfully(){

        //Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.of(savedEntity));

        // Act
        List<BankResponseDTO> actual1 = service.getBankDetails("HDBC0000001",null);

        //Assert
        Assertions.assertEquals("HDBC Bank", actual1.get(0).getBankName());
        Assertions.assertEquals("HDBC0000001",actual1.get(0).getIfscCode());
        Assertions.assertEquals("Bhilai", actual1.get(0).getBranch());

        Mockito.verify(repository, Mockito.times(1)).
                findByIfscCode("HDBC0000001");
    }

    @Test
    void shouldReturnBankDetailsByBankIdFromDBSuccessfully(){

        //Arrange
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(savedEntity));

        // Act

        List<BankResponseDTO> actual = service.getBankDetails(
                null,1L
        );

        //Assert
        Assertions.assertEquals("HDBC Bank", actual.get(0).getBankName());
        Assertions.assertEquals("HDBC0000001",actual.get(0).getIfscCode());
        Assertions.assertEquals("Bhilai", actual.get(0).getBranch());


        Mockito.verify(repository, Mockito.times(1)).
                findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenBankIdAndIfscNotNullInGetBankDetails(){
        BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                ()->service.getBankDetails("HDBC0000001",1L));

        Assertions.assertEquals("Provide either IFSC or bankId", exception.getMessage());

        Mockito.verify(repository,Mockito.never()).findById(1L);
        Mockito.verify(repository,Mockito.never()).findByIfscCode(Mockito.any());
        Mockito.verify(repository,Mockito.never()).findAll();
    }

    @Test
    void shouldThrowInvalidIfscCodeExceptionWhenIfscLengthIsInvalid() {
        //Acts + Assert
        InvalidIfscCodeException ex = Assertions.assertThrows(
                InvalidIfscCodeException.class,
                () -> service.getBankDetails("HDBC123", null)
        );

        Assertions.assertEquals("Invalid IFSC Code", ex.getMessage());

        //Verify
        Mockito.verify(repository, Mockito.never()).findByIfscCode(Mockito.any());
        Mockito.verify(repository, Mockito.never()).findById(Mockito.any());
        Mockito.verify(repository, Mockito.never()).findAll();
    }

    @Test
    void shouldThrowBankNotFoundExceptionWhenBankNotFoundByIfsc(){

        //Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000002")).thenReturn(Optional.empty());

        //Act & Assert
        BankNotFoundException exception = Assertions.assertThrows(BankNotFoundException.class,
                ()->service.getBankDetails("HDBC0000002",null));


        Assertions.assertEquals("Bank not found",exception.getMessage());

        Mockito.verify(repository,Mockito.times(1)).findByIfscCode("HDBC0000002");
        Mockito.verify(repository, Mockito.never())
                .findById(Mockito.any());

        Mockito.verify(repository, Mockito.never())
                .findAll();
    }

    @Test
    void shouldThrowBankNotFoundExceptionWhenBankNotFoundByBankId(){

        //Arrange
        Mockito.when(repository.findById(1L)).thenReturn(Optional.empty());

        //Act & Assert
        BankNotFoundException exception = Assertions.assertThrows(BankNotFoundException.class,
                ()->service.getBankDetails(null,1L));


        Assertions.assertEquals("Bank not found",exception.getMessage());

        Mockito.verify(repository,Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(repository, Mockito.never())
                .findByIfscCode(Mockito.any());

        Mockito.verify(repository, Mockito.never())
                .findAll();
    }



    @Test
    void shouldReturnCustomersSuccessfully() {
        // Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.of(savedEntity));

        Mockito.when(customerClient.getCustomers(1L, null, null))
                .thenReturn(List.of(customerDTO));

        // Act
        List<CustomerBalanceDTO> result =
                service.getCustomersByIfsc("HDBC0000001", null, null);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Ritik", result.get(0).getName());

        Mockito.verify(customerClient).getCustomers(1L, null, null);
    }

    @Test
    void shouldThrowBankNotFoundExceptionWhenIfscInvalid(){
        // Arrange
        Mockito.when(repository.findByIfscCode(savedEntity.getIfscCode())).thenReturn(Optional.empty());

        // Act + Assert
        BankNotFoundException ex = Assertions.assertThrows(BankNotFoundException.class,
                        () -> service.getCustomersByIfsc(savedEntity.getIfscCode(), null, null));

        Assertions.assertEquals("Invalid IFSC", ex.getMessage());

        Mockito.verify(customerClient, Mockito.never()).getCustomers(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowCustomerNotFoundWhenFeignNotFoundOccurs() {
        // Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.of(savedEntity));

        Mockito.when(customerClient.getCustomers(1L, null, null))
                .thenThrow(Mockito.mock(feign.FeignException.NotFound.class));

        // Act + Assert
        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                        () -> service.getCustomersByIfsc("HDBC0000001", null, null));

        Assertions.assertEquals("No customers found", ex.getMessage());
    }

    @Test
    void shouldThrowCustomerNotFoundWhenCustomerListIsEmpty() {
        // Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.of(savedEntity));

        Mockito.when(customerClient.getCustomers(1L, null, null))
                .thenReturn(Collections.emptyList());

        // Act + Assert
        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                        () -> service.getCustomersByIfsc("HDBC0000001", null, null));

        Assertions.assertEquals("No customers found", ex.getMessage());
    }

    @Test
    void shouldThrowCustomerNotFoundWhenCustomerListIsNull() {
        // Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.of(savedEntity));

        Mockito.when(customerClient.getCustomers(1L, null, null)).thenReturn(null);

        // Act + Assert
        CustomerNotFoundException ex = Assertions.assertThrows(CustomerNotFoundException.class,
                        () -> service.getCustomersByIfsc("HDBC0000001", null, null));

        Assertions.assertEquals("No customers found", ex.getMessage());
    }

    @Test
    void shouldPassCorrectParametersToCustomerClient() {
        // Arrange
        BigDecimal min = BigDecimal.valueOf(1000);
        BigDecimal max = BigDecimal.valueOf(10000);

        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.of(savedEntity));

        Mockito.when(customerClient.getCustomers(1L, min, max)).thenReturn(List.of(customerDTO));

        // Act
        service.getCustomersByIfsc("HDBC0000001", min, max);

        // Assert
        Mockito.verify(customerClient).getCustomers(1L, min, max);
    }


}
