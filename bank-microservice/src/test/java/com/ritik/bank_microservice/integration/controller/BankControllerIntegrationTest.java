package com.ritik.bank_microservice.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritik.bank_microservice.dto.BankRequestDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BankControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterBankSuccessfully() throws Exception {

        BankRequestDTO request = new BankRequestDTO();
        request.setBankName("HDFC Bank");
        request.setIfscCode("HDFC0123456");
        request.setBranch("Ahmedabad");

        mockMvc.perform(post("/api/banks/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bankName").value("HDFC Bank"))
                .andExpect(jsonPath("$.ifscCode").value("HDFC0123456"));
    }
}

