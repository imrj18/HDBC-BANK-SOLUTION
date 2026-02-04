package com.ritik.customer_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritik.customer_microservice.config.JwtFilter;
import com.ritik.customer_microservice.config.MethodSecurityConfig;
import com.ritik.customer_microservice.controller.InternalCustomerController;
import com.ritik.customer_microservice.dto.customerDTO.CustomerBalanceDTO;
import com.ritik.customer_microservice.service.CustomerService;
import com.ritik.customer_microservice.serviceImpl.PageResponse;
import com.ritik.customer_microservice.serviceImpl.TransactionFailureService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalCustomerController.class)
@Import(MethodSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private TransactionFailureService transactionFailureService;

    @MockBean
    private CustomerService customerService;

    @Test
    @WithMockUser(roles = "SERVICE")
    void shouldReturnCustomersSuccessfully() throws Exception {

        CustomerBalanceDTO dto = new CustomerBalanceDTO();
        dto.setCustomerId(UUID.randomUUID());
        dto.setBalance(BigDecimal.valueOf(25000));

        Mockito.when(customerService.fetchCustomersByBankIdAndBalance(1L, null,
                        null,0,5))
                .thenReturn(new PageResponse<>(List.of(dto),0,1,5,true));

        mockMvc.perform(get("/internal/customers")
                        .param("bankId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].customerId").value(dto.getCustomerId().toString()))
                .andExpect(jsonPath("$.data[0].balance").value(25000));

        Mockito.verify(customerService).fetchCustomersByBankIdAndBalance(1L, null,
                null,0,5);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenRoleIsNotService() throws Exception {

        mockMvc.perform(get("/internal/customers").param("bankId", "1"))
                .andExpect(status().isForbidden());

        Mockito.verify(customerService, Mockito.never()).fetchCustomersByBankIdAndBalance(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    @WithMockUser(roles = "SERVICE")
    void shouldReturnCustomersWithBalanceFilter() throws Exception {

        UUID customerId = UUID.randomUUID();

        CustomerBalanceDTO dto = new CustomerBalanceDTO();
        dto.setCustomerId(customerId);
        dto.setBalance(BigDecimal.valueOf(40000));

        Mockito.when(customerService.fetchCustomersByBankIdAndBalance(
                        1L,
                        BigDecimal.valueOf(20000),
                        BigDecimal.valueOf(50000), 0,
                        5))
                .thenReturn(new PageResponse<>(List.of(dto),0,1,5,true));

        mockMvc.perform(get("/internal/customers")
                        .param("bankId", "1")
                        .param("minBalance", "20000")
                        .param("maxBalance", "50000")
                        .param("page","0")
                        .param("size","5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.data[0].balance").value(40000));

        Mockito.verify(customerService)
                .fetchCustomersByBankIdAndBalance(1L, BigDecimal.valueOf(20000),
                        BigDecimal.valueOf(50000),0,5);
    }

}

