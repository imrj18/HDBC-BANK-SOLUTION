package com.ritik.customer_microservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritik.customer_microservice.config.JwtFilter;
import com.ritik.customer_microservice.config.WithMockCustomer;
import com.ritik.customer_microservice.controller.CustomerController;
import com.ritik.customer_microservice.dto.customerDTO.*;
import com.ritik.customer_microservice.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRegisterDTO registerDTO;
    private CustomerLoginDTO loginDTO;
    private CustomerResponseDTO responseDTO;
    private AuthResponseDTO authResponseDTO;

    @BeforeEach
    void setUp(){
        registerDTO = new CustomerRegisterDTO();
        registerDTO.setName("John Doe");
        registerDTO.setPhone("9876543210");
        registerDTO.setAddress("HIG-1/299, Vidya Vihar, Shanti Nagar, Durg");
        registerDTO.setAadhar("123456789012");
        registerDTO.setPassword("password");
        registerDTO.setEmail("jd@gmail.com");

        responseDTO = new CustomerResponseDTO();
        responseDTO.setCustomerId(UUID.randomUUID().toString());
        responseDTO.setName("John Doe");
        responseDTO.setEmail("jd@gmail.com");
        responseDTO.setPhone("9876543210");

        loginDTO = new CustomerLoginDTO();
        loginDTO.setEmail("jd@gmail.com");
        loginDTO.setPassword("J@1234");

        authResponseDTO = new AuthResponseDTO();
        authResponseDTO.setToken("jwt-token");
        authResponseDTO.setTokenType("Bearer");
        authResponseDTO.setExpiresIn(10L);

    }

    @Test
    void shouldRegisterCustomerSuccessfully() throws Exception {

        //Arrange
        Mockito.when(customerService.register(Mockito.any(CustomerRegisterDTO.class))).thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(post("/api/customers/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(jsonPath("$.customerId").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("jd@gmail.com"))
                .andExpect(jsonPath("$.phone").value("9876543210"));

        //Verify
        Mockito.verify(customerService).register(Mockito.any());
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        //Arrange
        registerDTO.setEmail("invalid-email");

        //Act + Assert
        mockMvc.perform(
                        post("/api/customers/register").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());

        Mockito.verify(customerService, Mockito.never()).register(Mockito.any());
    }

    @Test
    void shouldReturnBadRequestWhenNameIsMissing() throws Exception {
        //Arrange
        registerDTO.setName(null);

        //Act + Assert
        mockMvc.perform(
                        post("/api/customers/register").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());

        Mockito.verify(customerService, Mockito.never()).register(Mockito.any());
    }

    @Test
    void shouldLoginCustomerSuccessfully() throws Exception {

        // Arrange
        Mockito.when(customerService.verify(Mockito.any(CustomerLoginDTO.class))).thenReturn(authResponseDTO);

        // Act & Assert
        mockMvc.perform(
                        post("/api/customers/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(10L));

        Mockito.verify(customerService).verify(Mockito.any(CustomerLoginDTO.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalidDuringLogin() throws Exception {

        // Arrange
        loginDTO.setEmail("invalid-email");

        Mockito.when(customerService.verify(Mockito.any(CustomerLoginDTO.class))).thenReturn(authResponseDTO);

        // Act & Assert
        mockMvc.perform(
                        post("/api/customers/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());

        Mockito.verify(customerService, Mockito.never()).verify(Mockito.any(CustomerLoginDTO.class));
    }


    @Test
    @WithMockCustomer()
    void shouldReturnCustomerProfileSuccessfully() throws Exception {

        //Arrange
        Mockito.when(customerService.viewProfile("jd@gmail.com")).thenReturn(responseDTO);

        //Act + Assert
        mockMvc.perform(get("/api/customers/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jd@gmail.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        Mockito.verify(customerService, Mockito.times(1)).viewProfile("jd@gmail.com");
    }
//    Golden rules
//    @WithMockUser ≠ custom principal
//    If controller casts principal → use custom security context
//    Controller tests should mock security context, not JWT

    @Test
    @WithMockCustomer()
    void shouldUpdateCustomerProfileSuccessfully() throws Exception {

        // Arrange
        CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
        updateDTO.setName("John Updated");
        updateDTO.setAddress("New Address, Bhilai");
        updateDTO.setPhone("9876543210");

        responseDTO.setEmail("jd@gmail.com");
        responseDTO.setName("John Updated");

        Mockito.when(customerService.updateProfile(
                Mockito.eq("jd@gmail.com"),
                Mockito.any(CustomerUpdateDTO.class)
        )).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(
                        put("/api/customers/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jd@gmail.com"))
                .andExpect(jsonPath("$.name").value("John Updated"));

        Mockito.verify(customerService)
                .updateProfile(
                        Mockito.eq("jd@gmail.com"),
                        Mockito.any(CustomerUpdateDTO.class)
                );
    }

    @Test
    @WithMockCustomer()
    void shouldReturnBadRequestWhenUpdateDataIsInvalid() throws Exception {

        // Arrange
        CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
        updateDTO.setName("");
        updateDTO.setAddress("Some address");

        // Act & Assert
        mockMvc.perform(
                        put("/api/customers/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO))
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(customerService, Mockito.never()).updateProfile(Mockito.any(), Mockito.any());
    }

}
