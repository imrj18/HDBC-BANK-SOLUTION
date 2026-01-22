package com.ritik.customer_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritik.customer_microservice.config.JwtFilter;
import com.ritik.customer_microservice.config.WithMockCustomer;
import com.ritik.customer_microservice.dto.transactionDTO.*;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.enums.TransactionType;
import com.ritik.customer_microservice.service.AccountService;
import com.ritik.customer_microservice.service.OtpService;
import com.ritik.customer_microservice.service.TransactionService;
import com.ritik.customer_microservice.serviceImpl.PageResponse;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private OtpService otpService;

    private DepositRequestDTO depositRequestDTO;
    private TransactionResponseDTO transactionResponseDTO;
    private WithdrawRequestDTO withdrawRequestDTO;
    private TransferRequestDTO transferRequestDTO;

    @BeforeEach
    void setup() {

        depositRequestDTO = new DepositRequestDTO();
        depositRequestDTO.setAccountNum(12345678901L);
        depositRequestDTO.setAmount(BigDecimal.valueOf(5000));

        withdrawRequestDTO = new WithdrawRequestDTO();
        withdrawRequestDTO.setAccountNum(12345678901L);
        withdrawRequestDTO.setPin("1234");
        withdrawRequestDTO.setAmount(BigDecimal.valueOf(5000));

        transferRequestDTO = new TransferRequestDTO();
        transferRequestDTO.setFromAccountNum(12345678901L);
        transferRequestDTO.setToAccountNum(98765432109L);
        transferRequestDTO.setAmount(BigDecimal.valueOf(2000));
        transferRequestDTO.setPin("1234");

        transactionResponseDTO = new TransactionResponseDTO();
        transactionResponseDTO.setAccountNum(12345678901L);
        transactionResponseDTO.setAmount(BigDecimal.valueOf(5000));
        transactionResponseDTO.setTransactionStatus(TransactionStatus.PENDING);
    }

    @Test
    @WithMockCustomer()
    void shouldDepositMoneySuccessfully() throws Exception {

        Mockito.when(transactionService.depositMoney(
                        Mockito.eq("jd@gmail.com"),
                        Mockito.any(DepositRequestDTO.class)))
                .thenReturn(transactionResponseDTO);

        mockMvc.perform(post("/api/customers/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.accountNum").value(12345678901L))
                .andExpect(jsonPath("$.amount").value(5000));

        Mockito.verify(transactionService)
                .depositMoney(Mockito.eq("jd@gmail.com"), Mockito.any(DepositRequestDTO.class));
    }

    @Test
    @WithMockCustomer()
    void shouldReturn400WhenDepositRequestInvalid() throws Exception {

        mockMvc.perform(post("/api/customers/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DepositRequestDTO())))
                .andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.never()).depositMoney(Mockito.any(), Mockito.any());
    }

    @Test
    @WithMockCustomer()
    void shouldWithdrawMoneySuccessfully() throws Exception {

        Mockito.when(transactionService.withdrawMoney(
                        Mockito.eq("jd@gmail.com"),
                        Mockito.any(WithdrawRequestDTO.class)))
                .thenReturn(transactionResponseDTO);

        mockMvc.perform(post("/api/customers/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.accountNum").value(12345678901L))
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.transactionStatus").value("PENDING"));

        Mockito.verify(transactionService)
                .withdrawMoney(Mockito.eq("jd@gmail.com"), Mockito.any(WithdrawRequestDTO.class));
    }

    @Test
    @WithMockCustomer()
    void shouldReturn400WhenWithdrawRequestInvalid() throws Exception {

        mockMvc.perform(post("/api/customers/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WithdrawRequestDTO())))
                .andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.never()).withdrawMoney(Mockito.any(), Mockito.any());
    }

    @Test
    @WithMockCustomer()
    void shouldReturnTransactionHistorySuccessfully() throws Exception {

        TransactionHistoryDTO historyDTO = new TransactionHistoryDTO();
        historyDTO.setTransactionType(TransactionType.DEBIT);
        historyDTO.setAmount(BigDecimal.valueOf(5000));

        Mockito.when(transactionService.transactionHistory("jd@gmail.com", null,0,5))
                .thenReturn(new PageResponse<>(List.of(historyDTO),0,1,5,true));

        mockMvc.perform(get("/api/customers/transactions/transactionHistory")
                        .param("page","0")
                        .param("size","5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionType").value("DEBIT"))
                .andExpect(jsonPath("$.data[0].amount").value(5000));

        Mockito.verify(transactionService).transactionHistory("jd@gmail.com", null,0,5);
    }

    @Test
    @WithMockCustomer()
    void shouldTransferMoneySuccessfully() throws Exception {

        TransferResponseDTO responseDTO = new TransferResponseDTO();
        responseDTO.setAmount(BigDecimal.valueOf(2000));
        responseDTO.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionService.transferMoney(
                        Mockito.eq("jd@gmail.com"),
                        Mockito.any(TransferRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/customers/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.amount").value(2000))
                .andExpect(jsonPath("$.status").value("PENDING"));

        Mockito.verify(transactionService).transferMoney(Mockito.eq("jd@gmail.com"), Mockito.any());
    }

    @Test
    @WithMockCustomer()
    void shouldReturn400WhenTransferRequestInvalid() throws Exception {

        mockMvc.perform(post("/api/customers/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequestDTO())))
                .andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.never()).transferMoney(Mockito.any(), Mockito.any());
    }

    @Test
    @WithMockCustomer()
    void shouldConfirmTransactionSuccessfully() throws Exception{

        ConfirmRequestDTO confirmRequestDTO = new ConfirmRequestDTO();
        confirmRequestDTO.setTransactionId(UUID.randomUUID());
        confirmRequestDTO.setOTP("1234");

        transactionResponseDTO.setTransactionStatus(TransactionStatus.SUCCESS);

        Mockito.when(transactionService.transactionConfirm(
                        Mockito.eq("jd@gmail.com"),
                        Mockito.any(ConfirmRequestDTO.class)))
                .thenReturn(transactionResponseDTO);

        mockMvc.perform(post("/api/customers/transactions/confirm-transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmRequestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.transactionStatus").value("SUCCESS"));

        Mockito.verify(transactionService).transactionConfirm(Mockito.eq("jd@gmail.com"), Mockito.any());
    }

    @Test
    @WithMockCustomer()
    void shouldReturn400WhenConfirmTransactionRequestInvalid() throws Exception {

        mockMvc.perform(post("/api/customers/transactions/confirm-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmRequestDTO())))
                .andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.never()).transactionConfirm(Mockito.any(), Mockito.any());
    }
}
