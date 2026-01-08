package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.customerDTO.*;
import com.ritik.customer_microservice.enums.Status;
import com.ritik.customer_microservice.exception.AlreadyLoggedInException;
import com.ritik.customer_microservice.exception.CustomerAlreadyExistsException;
import com.ritik.customer_microservice.exception.CustomerNotFoundException;
import com.ritik.customer_microservice.exception.WrongPasswordException;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.CustomerSession;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.repository.CustomerSessionRepository;
import com.ritik.customer_microservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtServiceImpl jwtService;

    private final CustomerSessionRepository customerSessionRepository;

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

    private Customer toEntity(CustomerRegisterDTO dto) {
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

        Customer customer = toEntity(registerDTO);


        Customer savedCustomer = customerRepository.save(customer);
        return toResponseDto(savedCustomer);
    }

    @Override
    public AuthResponseDTO verify(CustomerLoginDTO dto) {

        Customer customer = customerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with given email"));

        if (!passwordEncoder.matches(dto.getPassword(), customer.getPasswordHash())) {
            throw new WrongPasswordException("Invalid password");
        }

        customerSessionRepository.findById(customer.getCustomerId())
                .ifPresent(session -> {
                    if (session.getExpiryTime().isAfter(LocalDateTime.now())) {
                        throw new AlreadyLoggedInException("Customer already has an active session");
                    } else {
                        customerSessionRepository.delete(session);
                    }
                });

        String token = jwtService.generateUserToken(dto.getEmail());

        Date expiryDate = jwtService.extractExpiryTime(token);
        LocalDateTime expiryTime = expiryDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        CustomerSession session = new CustomerSession();
        session.setCustomer(customer);
        session.setToken(token);
        session.setExpiryTime(expiryTime);
        session.setLastActivityTime(LocalDateTime.now());

        customerSessionRepository.save(session);

        return new AuthResponseDTO(
                token,
                "Bearer",
                Duration.between(LocalDateTime.now(), expiryTime).getSeconds()
        );
    }



    @Override
    public CustomerResponseDTO viewProfile(String email) {
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() ->
                new CustomerNotFoundException("Customer not found."));
        return toResponseDto(customer);
    }

    @Override
    public CustomerResponseDTO updateProfile(String email, CustomerUpdateDTO updateDTO) {
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() ->
                new CustomerNotFoundException("Customer not found."));
        if(customerRepository.existsByPhone(updateDTO.getPhone())){
            throw new CustomerAlreadyExistsException("Phone number already exists!");
        }
        customer.setName(updateDTO.getName());
        customer.setPhone(updateDTO.getPhone());
        customer.setAddress(updateDTO.getAddress());
        customerRepository.save(customer);
        return toResponseDto(customer);
    }


    @Override
    public PageResponse<CustomerBalanceDTO> fetchCustomersByBankIdAndBalance(
            Long bankId,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Object[]> results = customerRepository
                .findCustomersByBankIdAndBalance(bankId, minBalance, maxBalance,pageable);

        if (results.isEmpty()) {
            throw new CustomerNotFoundException("No customers found");
        }

        List<CustomerBalanceDTO> data = new ArrayList<>();

        for (Object[] row : results.getContent()) {
            Customer customer = (Customer) row[0];
            BigDecimal balance = (BigDecimal) row[1];

            data.add(new CustomerBalanceDTO(
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getEmail(),
                    balance
            ));
        }

        return new PageResponse<>(
                data,
                results.getNumber(),
                results.getTotalPages(),
                results.getTotalElements(),
                results.isLast()
        );
    }
}
