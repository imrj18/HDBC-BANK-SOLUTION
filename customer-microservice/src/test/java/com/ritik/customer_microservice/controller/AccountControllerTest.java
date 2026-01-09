package com.ritik.customer_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritik.customer_microservice.config.JwtFilter;
import com.ritik.customer_microservice.config.WithMockCustomer;
import com.ritik.customer_microservice.controller.AccountController;
import com.ritik.customer_microservice.dto.accountDTO.AccountBalanceDTO;
import com.ritik.customer_microservice.dto.accountDTO.AccountResponseDTO;
import com.ritik.customer_microservice.dto.accountDTO.CreateAccountDTO;
import com.ritik.customer_microservice.enums.AccountType;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.exception.BankNotFoundException;
import com.ritik.customer_microservice.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateAccountDTO accountDTO;
    private AccountResponseDTO responseDTO;

    @BeforeEach
    void setup(){

        accountDTO = new CreateAccountDTO();
        accountDTO.setIfscCode("HDBC0000001");
        accountDTO.setAccountType(AccountType.Saving);
        accountDTO.setPin("1234");

        responseDTO = new AccountResponseDTO();
        responseDTO.setAccountNum(12345678901L);
        responseDTO.setAccountStatus(Status.ACTIVE);
        responseDTO.setBankId(1L);
        responseDTO.setAccountType(AccountType.Saving);
    }

    @Test
    @WithMockCustomer()
    void shouldCreateAccountSuccessfully() throws Exception {

        // Arrange
        Mockito.when(accountService.createAccount(
                Mockito.eq("jd@gmail.com"),
                Mockito.any(CreateAccountDTO.class)
        )).thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(post("/api/customers/accounts/create-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNum").value(12345678901L))
                .andExpect(jsonPath("$.bankId").value(1L))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

        Mockito.verify(accountService, Mockito.times(1))
                .createAccount(Mockito.eq("jd@gmail.com"), Mockito.any(CreateAccountDTO.class));
    }

//    @Test
//    void shouldReturn401WhenUserNotAuthenticated() throws Exception {
//
//        mockMvc.perform(
//                        post("/api/customers/accounts/create-account")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(accountDTO))
//                )
//                .andExpect(status().isForbidden());
//
//        Mockito.verify(accountService, Mockito.never())
//                .createAccount(Mockito.any(), Mockito.any());
//    }

    @Test
    @WithMockCustomer
    void shouldReturn400WhenRequestBodyIsInvalid() throws Exception {
        //Arrange
        CreateAccountDTO invalidDTO = new CreateAccountDTO();

        //Act + Assert
        mockMvc.perform(post("/api/customers/accounts/create-account")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        Mockito.verify(accountService, Mockito.never()).createAccount(Mockito.any(), Mockito.any());
    }

    @Test
    @WithMockCustomer()
    void shouldReturn404WhenBankNotFound() throws Exception {

        //Arrange
        Mockito.when(accountService.createAccount(Mockito.anyString(), Mockito.any(CreateAccountDTO.class)))
                .thenThrow(new BankNotFoundException("Bank not found"));

        //Act + Assert
        mockMvc.perform(post("/api/customers/accounts/create-account")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bank not found"));

        Mockito.verify(accountService, Mockito.times(1))
                .createAccount(Mockito.anyString(), Mockito.any(CreateAccountDTO.class));
    }

    @Test
    @WithMockCustomer()
    void shouldReturnAccountBalanceSuccessfully() throws Exception {

        // Arrange
        AccountBalanceDTO balanceDTO = new AccountBalanceDTO();
        balanceDTO.setAccountNumber(12345678901L);
        balanceDTO.setAccountBalance(BigDecimal.valueOf(25000));

        Mockito.when(accountService.checkBalance(Mockito.eq("jd@gmail.com"), Mockito.eq(12345678901L)))
                .thenReturn(balanceDTO);

        // Act & Assert
        mockMvc.perform(
                        get("/api/customers/accounts/{accountNum}/check-balance", 12345678901L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(12345678901L))
                .andExpect(jsonPath("$.accountBalance").value(25000));

        Mockito.verify(accountService, Mockito.times(1))
                .checkBalance("jd@gmail.com", 12345678901L);
    }

    @Test
    @WithMockCustomer()
    void shouldReturnAllAccountInfoSuccessfully() throws Exception {

        // Arrange
        AccountResponseDTO account1 = new AccountResponseDTO();
        account1.setAccountNum(11111111111L);
        account1.setBankId(1L);

        AccountResponseDTO account2 = new AccountResponseDTO();
        account2.setAccountNum(22222222222L);
        account2.setBankId(1L);

        List<AccountResponseDTO> responseList = List.of(account1, account2);

        Mockito.when(accountService.getAccountInfo("jd@gmail.com", null)).thenReturn(responseList);

        // Act & Assert
        mockMvc.perform(get("/api/customers/accounts/account-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountNum").value(11111111111L))
                .andExpect(jsonPath("$[1].accountNum").value(22222222222L));

        Mockito.verify(accountService, Mockito.times(1))
                .getAccountInfo("jd@gmail.com", null);
    }

    @Test
    @WithMockCustomer()
    void shouldReturnAccountInfoByAccountNumSuccessfully() throws Exception {

        // Arrange
        AccountResponseDTO account = new AccountResponseDTO();
        account.setAccountNum(12345678901L);
        account.setBankId(1L);

        List<AccountResponseDTO> responseList = List.of(account);

        Mockito.when(accountService.getAccountInfo("jd@gmail.com", 12345678901L))
                .thenReturn(responseList);

        // Act & Assert
        mockMvc.perform(get("/api/customers/accounts/account-info")
                                .param("accountNum", "12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].accountNum").value(12345678901L));

        Mockito.verify(accountService, Mockito.times(1))
                .getAccountInfo("jd@gmail.com", 12345678901L);
    }


}
