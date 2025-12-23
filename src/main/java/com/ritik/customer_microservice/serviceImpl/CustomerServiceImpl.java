package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.CustomerLoginDTO;
import com.ritik.customer_microservice.dto.CustomerRegisterDTO;
import com.ritik.customer_microservice.dto.CustomerResponseDTO;
import com.ritik.customer_microservice.enums.BankStatus;
import com.ritik.customer_microservice.exception.CustomerAlreadyExistsException;
import com.ritik.customer_microservice.exception.CustomerNotFoundException;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private CustomerRepository customerRepository;

    private PasswordEncoder passwordEncoder;

    private JwtServiceImpl jwtService;

    public CustomerServiceImpl(CustomerRepository repository, PasswordEncoder passwordEncoder,
                               JwtServiceImpl jwtService) {

        this.customerRepository = repository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    private static CustomerResponseDTO toResponseDto(Customer customer) {

        CustomerResponseDTO dto = new CustomerResponseDTO();

        dto.setCustomerId(customer.getCustomerId().toString());
        dto.setBankId(customer.getBankId());

        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAadhar(customer.getAadhar());

        dto.setBankStatus(customer.getBankStatus().name());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        return dto;
    }

    private static Customer toEntity(
            CustomerRegisterDTO dto,
            PasswordEncoder passwordEncoder
    ) {
        Customer customer = new Customer();

        customer.setBankId(dto.getBankId());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAadhar(dto.getAadhar());

        customer.setPasswordHash(
                passwordEncoder.encode(dto.getPassword())
        );

        customer.setBankStatus(BankStatus.ACTIVE);

        return customer;
    }

    public CustomerResponseDTO register(CustomerRegisterDTO registerDTO) {

        if (customerRepository.existsByEmailAndBankId(
                registerDTO.getEmail(), registerDTO.getBankId())) {

            throw new CustomerAlreadyExistsException("Customer already exists");
        }

        Customer customer = toEntity(registerDTO, passwordEncoder);
        Customer savedCustomer = customerRepository.save(customer);

        return toResponseDto(savedCustomer);
    }


    public String verify(CustomerLoginDTO dto) {

        log.info("Inside verify service logic");

        Customer customer = customerRepository
                .findByEmailAndBankId(dto.getEmail(), dto.getBankId())
                .orElseThrow(() -> new BadCredentialsException("Invalid bank or email"));

        if (!passwordEncoder.matches(dto.getPassword(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password");
        }

        return jwtService.generateToken(dto.getBankId(), dto.getEmail());
    }

}
