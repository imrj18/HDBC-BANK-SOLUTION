package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.CustomerLoginDTO;
import com.ritik.customer_microservice.dto.CustomerRegisterDTO;
import com.ritik.customer_microservice.dto.CustomerResponseDTO;
import com.ritik.customer_microservice.dto.CustomerUpdateDTO;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.exception.CustomerAlreadyExistsException;
import com.ritik.customer_microservice.exception.CustomerNotFoundException;
import com.ritik.customer_microservice.exception.WrongPasswordException;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtServiceImpl jwtService;

    private static CustomerResponseDTO toResponseDto(Customer customer) {

        CustomerResponseDTO dto = new CustomerResponseDTO();

        dto.setCustomerId(customer.getCustomerId().toString());

        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setAadhar(customer.getAadhar());

        dto.setBankStatus(customer.getStatus().name());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        return dto;
    }

    private static Customer toEntity(CustomerRegisterDTO dto, PasswordEncoder passwordEncoder) {
        Customer customer = new Customer();

        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setAadhar(dto.getAadhar());

        customer.setPasswordHash(
                passwordEncoder.encode(dto.getPassword())
        );

        customer.setStatus(Status.ACTIVE);

        return customer;
    }

    @Override
    public CustomerResponseDTO register(CustomerRegisterDTO registerDTO) {

        if (customerRepository.existsByEmail(registerDTO.getEmail())) {
            throw new CustomerAlreadyExistsException("Email already exists");
        }

        if (customerRepository.existsByAadhar(registerDTO.getAadhar())) {
            throw new CustomerAlreadyExistsException("Aadhaar already exists");
        }

        if (customerRepository.existsByPhone(registerDTO.getPhone())) {
            throw new CustomerAlreadyExistsException("Phone already exists");
        }

        Customer customer = toEntity(registerDTO, passwordEncoder);


        Customer savedCustomer = customerRepository.save(customer);
        return toResponseDto(savedCustomer);
    }

    @Override
    public String verify(CustomerLoginDTO dto) {

        Customer customer = customerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found!!! Invalid email"));

        if (!passwordEncoder.matches(dto.getPassword(), customer.getPasswordHash())) {
            throw new WrongPasswordException ("Wrong password");
        }

        return jwtService.generateToken(dto.getEmail());
    }

    @Override
    public CustomerResponseDTO viewProfile(String email){
        Customer customer = customerRepository.findByEmail(email).orElseThrow(()->
                new CustomerNotFoundException("Customer not found."));
        return toResponseDto(customer);
    }

    @Override
    public CustomerResponseDTO updateProfile(String email,CustomerUpdateDTO updateDTO) {
        Customer customer = customerRepository.findByEmail(email).orElseThrow(()->
                new CustomerNotFoundException("Customer not found."));
        customer.setName(updateDTO.getName());
        customer.setPhone(updateDTO.getPhone());
        customer.setAddress(updateDTO.getAddress());
        customerRepository.save(customer);
        return toResponseDto(customer);
    }
}
