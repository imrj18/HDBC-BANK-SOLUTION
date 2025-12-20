package com.ritik.bank_microservice.serviceImpl;


import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.exception.BadRequestException;
import com.ritik.bank_microservice.exception.ResourceNotFoundException;
import com.ritik.bank_microservice.model.Bank;
import com.ritik.bank_microservice.repository.BankRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BankServiceImplTest {

    @Mock
    private BankRepository repository;

    @InjectMocks
    private BankServiceImpl service;

    public BankRequestDTO createDTO(){
        BankRequestDTO dto = new BankRequestDTO();
        dto.setBankName("HDBC Bank");
        dto.setIfscCode("HDBC0000001");
        dto.setBranch("Bhilai");
        return dto;
    }

    @Test
    void shouldRegisterBankInDBSuccessfully(){

        //Arrange

        Bank savedEntity = new Bank(1L,"HDBC Bank","HDBC0000001",
                "Bhilai",LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(repository.findByIfscCode("HDBC0000001"))
                .thenReturn(Optional.empty());

        Mockito.when(repository.save(Mockito.any(Bank.class)))
                .thenReturn(savedEntity);

        // Act
        BankResponseDTO actual = service.addBank(createDTO());


        //Assert
        Assertions.assertEquals(1L,actual.getBankId());
        Assertions.assertEquals("HDFC Bank", actual.getBankName());
        Assertions.assertEquals("HDBC0000001", actual.getIfscCode());
        Assertions.assertEquals("Bhilai", actual.getBranch());

        Mockito.verify(repository, Mockito.times(1))
                .save(Mockito.any(Bank.class));
        Mockito.verify(repository, Mockito.times(1))
                .findByIfscCode("HDBC0000001");
    }

    @Test
    void shouldThrowExceptionWhenIfscInvalid(){

        BankRequestDTO dto = new BankRequestDTO();
        dto.setBranch("Bhilai");
        dto.setBankName("HDBC Bank");
        dto.setIfscCode("HDBC00001");
        //Act & Assert
        BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                ()->service.addBank(dto));

        Assertions.assertEquals("Invalid IFSC Code", exception.getMessage());

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenIfsccodeAlreadyExists(){

        //Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000001"))
                .thenReturn(Optional.of(new Bank()));

        //Act & Assert
        BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                ()->service.addBank(createDTO()));

        Assertions.assertEquals("Ifsc code already exists.",exception.getMessage());

        Mockito.verify(repository,Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenAnyFieldEmpty(){

        BankRequestDTO dto = new BankRequestDTO();
        dto.setBranch("");
        dto.setBankName("HDBC Bank");
        dto.setIfscCode("HDBC0000001");

        //Act & Assert
        BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                ()->service.addBank(dto));

        Assertions.assertEquals("All Fields Required", exception.getMessage());

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldGetAllBankDetailsFromDBSuccessfully(){

        //Arrange
        Bank savedEntity = new Bank(1L,"HDBC Bank","HDBC0000001",
                "Bhilai",LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(repository.findAll()).thenReturn(List.of(savedEntity));

//        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.of(savedEntity));
//
//        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(savedEntity));

        // Act
//        List<Bank> actual1 = service.getBankDetails(
//                "HDBC0000001",null
//        );
//        List<Bank> actual2 = service.getBankDetails(
//                null,1L
//        );

        List<BankResponseDTO> actual = service.getBankDetails(null,null);

        //Assert

//        Assertions.assertEquals("HDBC Bank", actual1.get(0).getBankName());
//        Assertions.assertEquals("HDBC0000001",actual1.get(0).getIfscCode());
//        Assertions.assertEquals("Bhilai", actual1.get(0).getBranch());
//
//        Assertions.assertEquals("HDBC Bank", actual2.get(0).getBankName());
//        Assertions.assertEquals("HDBC0000001",actual2.get(0).getIfscCode());
//        Assertions.assertEquals("Bhilai", actual2.get(0).getBranch());

        Assertions.assertEquals("HDBC Bank", actual.get(0).getBankName());
        Assertions.assertEquals("HDBC0000001",actual.get(0).getIfscCode());
        Assertions.assertEquals("Bhilai", actual.get(0).getBranch());

//        Mockito.verify(repository, Mockito.times(1)).
//                findByIfscCode("HDBC0000001");
//
//        Mockito.verify(repository, Mockito.times(1)).
//                findById(1L);

        Mockito.verify(repository, Mockito.times(1)).
                findAll();
    }

    @Test
    void shouldGetBankDetailsByIfsccodeFromDBSuccessfully(){

        //Arrange
        Bank savedEntity = new Bank(1L,"HDBC Bank","HDBC0000001",
                "Bhilai",LocalDateTime.now(),LocalDateTime.now());


        Mockito.when(repository.findByIfscCode("HDBC0000001")).thenReturn(Optional.of(savedEntity));


        // Act
        List<BankResponseDTO> actual1 = service.getBankDetails(
                "HDBC0000001",null
        );


        //Assert

        Assertions.assertEquals("HDBC Bank", actual1.get(0).getBankName());
        Assertions.assertEquals("HDBC0000001",actual1.get(0).getIfscCode());
        Assertions.assertEquals("Bhilai", actual1.get(0).getBranch());


        Mockito.verify(repository, Mockito.times(1)).
                findByIfscCode("HDBC0000001");
    }

    @Test
    void shouldGetBankDetailsByBankIdFromDBSuccessfully(){

        //Arrange
        Bank savedEntity = new Bank(1L,"HDBC Bank","HDBC0000001",
                "Bhilai",LocalDateTime.now(),LocalDateTime.now());

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
    void shouldThrowExceptionWhenIdAndIfscNotNullInGetBankDetails(){
        BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                ()->service.getBankDetails("HDBC0000001",1L));

        Assertions.assertEquals("Please provide either IFSC or ID, not both", exception.getMessage());

        Mockito.verify(repository,Mockito.never()).findById(1L);
        Mockito.verify(repository,Mockito.never()).findByIfscCode(Mockito.any());
        Mockito.verify(repository,Mockito.never()).findAll();
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenBankNotFoundByIfsc(){

        //Arrange
        Mockito.when(repository.findByIfscCode("HDBC0000002")).thenReturn(Optional.empty());

        //Act & Assert
        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                ()->service.getBankDetails("HDBC0000002",null));


        Assertions.assertEquals("Bank not found with IFSC " + "HDBC0000002",exception.getMessage());

        Mockito.verify(repository,Mockito.times(1)).findByIfscCode("HDBC0000002");
        Mockito.verify(repository, Mockito.never())
                .findById(Mockito.any());

        Mockito.verify(repository, Mockito.never())
                .findAll();
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenBankNotFoundByBankId(){

        //Arrange
        Mockito.when(repository.findById(1L)).thenReturn(Optional.empty());

        //Act & Assert
        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                ()->service.getBankDetails(null,1L));


        Assertions.assertEquals("Bank not found with id " + 1L,exception.getMessage());

        Mockito.verify(repository,Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(repository, Mockito.never())
                .findByIfscCode(Mockito.any());

        Mockito.verify(repository, Mockito.never())
                .findAll();
    }

}
