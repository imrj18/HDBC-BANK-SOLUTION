package com.ritik.bank_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import com.ritik.bank_microservice.service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BankController.class)
class BankControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankService service;

    @Autowired
    private ObjectMapper objectMapper;

    private BankRequestDTO requestDTO;
    private BankResponseDTO responseDTO;
    private CustomerBalanceDTO customerBalanceDTO;

    @BeforeEach
    void setUp() {

        requestDTO = new BankRequestDTO();
        requestDTO.setBankName("HDBC Bank");
        requestDTO.setBranch("Mumbai");
        requestDTO.setIfscCode("HDBC0000001");

        responseDTO = new BankResponseDTO(
                1L,
                "HDBC Bank",
                "HDBC0000001",
                "Mumbai",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        customerBalanceDTO = new CustomerBalanceDTO();
        customerBalanceDTO.setName("Ritik Jani");
        customerBalanceDTO.setBalance(BigDecimal.valueOf(2000));
        customerBalanceDTO.setCustomerId(UUID.randomUUID());
    }

    @Test
    void shouldAddBankSuccessfully() throws Exception {

        Mockito.when(service.addBank(Mockito.any(BankRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/banks/register").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bankName").value("HDBC Bank"))
                .andExpect(jsonPath("$.ifscCode").value("HDBC0000001"))
                .andExpect(jsonPath("$.branch").value("Mumbai"));

        Mockito.verify(service).addBank(Mockito.any());
    }

    @Test
    void shouldReturnBadRequestWhenBankNameMissing() throws Exception {

        BankRequestDTO dto = new BankRequestDTO();
        dto.setIfscCode("HDBC000001");
        dto.setBranch("Bhilai");

        mockMvc.perform(post("/api/banks/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());

        Mockito.verify(service, Mockito.never()).addBank(Mockito.any());
    }

    @Test
    void shouldGetAllBanks() throws Exception {

        List<BankResponseDTO> responseList = List.of(
                new BankResponseDTO(1L, "HDBC Bank", "HDBC0000001",
                        "Mumbai", LocalDateTime.now(), LocalDateTime.now()),
                new BankResponseDTO(2L, "HDBC Bank", "HDBC0000002",
                        "Delhi", LocalDateTime.now(), LocalDateTime.now())
        );

        Mockito.when(service.getBankDetails(null, null)).thenReturn(responseList);

        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bankName").value("HDBC Bank"))
                .andExpect(jsonPath("$[1].bankName").value("HDBC Bank"));

        Mockito.verify(service).getBankDetails( null, null);
    }

    @Test
    void shouldGetBanksByIfsc() throws Exception {


        Mockito.when(service.getBankDetails("HDBC0000001", null)).
                thenReturn(Collections.singletonList(responseDTO));

        mockMvc.perform(get("/api/banks").param("ifsc", "HDBC0000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("HDBC Bank"));

        Mockito.verify(service).getBankDetails( "HDBC0000001", null);
    }

    @Test
    void shouldGetBanksByBankId() throws Exception {

        Mockito.when(service.getBankDetails(null, 1L))
                .thenReturn(Collections.singletonList(responseDTO));

        mockMvc.perform(get("/api/banks").param("bankId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("HDBC Bank"));

        Mockito.verify(service).getBankDetails(null,1L);
    }

    @Test
    void shouldGetCustomersByIfsc() throws Exception {


        List<CustomerBalanceDTO> response = List.of(customerBalanceDTO);

        Mockito.when(service.getCustomersByIfsc("HDBC0000001", null, null))
                .thenReturn(response);

        mockMvc.perform(get("/api/banks/customers")
                        .param("ifsc", "HDBC0000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].balance").isNumber());

        Mockito.verify(service).getCustomersByIfsc("HDBC0000001", null, null);
    }

    @Test
    void shouldGetCustomersByIfscAndMinBalance() throws Exception {

        Mockito.when(service.getCustomersByIfsc("HDBC0000001", BigDecimal.valueOf(1000), null))
                .thenReturn(List.of(customerBalanceDTO));

        mockMvc.perform(get("/api/banks/customers")
                        .param("ifsc", "HDBC0000001")
                        .param("minBalance", "1000"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(1))
                        .andExpect(jsonPath("$[0].name").isString())
                        .andExpect(jsonPath("$[0].balance").isNumber());

        Mockito.verify(service).getCustomersByIfsc("HDBC0000001", BigDecimal.valueOf(1000), null);
    }

    @Test
    void shouldGetCustomersByIfscAndMaxBalance() throws Exception {

        Mockito.when(service.getCustomersByIfsc("HDBC0000001", null, BigDecimal.valueOf(1000)))
                .thenReturn(List.of(customerBalanceDTO));

        mockMvc.perform(get("/api/banks/customers")
                        .param("ifsc", "HDBC0000001")
                        .param("maxBalance", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].balance").isNumber());

        Mockito.verify(service).getCustomersByIfsc("HDBC0000001", null, BigDecimal.valueOf(1000));
    }

    @Test
    void shouldGetCustomersByIfscMinAndMaxBalance() throws Exception {

        Mockito.when(service.getCustomersByIfsc("HDBC0000001",
                        BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(3000)))
                .thenReturn(List.of(customerBalanceDTO));

        mockMvc.perform(get("/api/banks/customers")
                        .param("ifsc", "HDBC0000001")
                        .param("minBalance", "1000")
                        .param("maxBalance", "3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].balance").isNumber());

        Mockito.verify(service).getCustomersByIfsc("HDBC0000001",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(3000)
        );
    }
}
