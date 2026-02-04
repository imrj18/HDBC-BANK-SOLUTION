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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtServiceImpl jwtService;

    private final CustomerSessionRepository customerSessionRepository;

    private static CustomerResponseDTO toResponseDto(Customer customer) {

        CustomerResponseDTO dto = new CustomerResponseDTO();

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
        log.info("Customer registration request received | email={}", registerDTO.getEmail());

        if (customerRepository.existsByEmail(registerDTO.getEmail())) {
            log.warn("Customer registration failed | reason=EMAIL_EXISTS | email={}", registerDTO.getEmail());
            throw new CustomerAlreadyExistsException("Email already exists");
        }

        if (customerRepository.existsByAadhar(registerDTO.getAadhar())) {
            log.warn("Customer registration failed | reason=AADHAAR_EXISTS | email={}", registerDTO.getEmail());
            throw new CustomerAlreadyExistsException("Aadhaar already exists");
        }

        if (customerRepository.existsByPhone(registerDTO.getPhone())) {
            log.warn("Customer registration failed | reason=PHONE_EXISTS | email={}", registerDTO.getEmail());
            throw new CustomerAlreadyExistsException("Phone already exists");
        }

        Customer customer = toEntity(registerDTO);


        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully | customerId={} | email={}",
                savedCustomer.getCustomerId(),
                savedCustomer.getEmail());
        return toResponseDto(savedCustomer);
    }

    @Override
    public AuthResponseDTO verify(CustomerLoginDTO dto) {
        log.info("Login attempt received | email={}", dto.getEmail());

        Customer customer = customerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed | reason=CUSTOMER_NOT_FOUND | email={}", dto.getEmail());
                    return new CustomerNotFoundException("Customer not found with given email");
                });
        if (!passwordEncoder.matches(dto.getPassword(), customer.getPasswordHash())) {
            log.warn("Login failed | reason=WRONG_PASSWORD | email={}", dto.getEmail());
            throw new WrongPasswordException("Invalid password");
        }

        customerSessionRepository.findById(customer.getCustomerId())
                .ifPresent(session -> {
                    if (session.getExpiryTime().isAfter(LocalDateTime.now())) {
                        log.warn(
                                "Login blocked | reason=ALREADY_LOGGED_IN | email={} | sessionExpiry={}",
                                dto.getEmail(),
                                session.getExpiryTime()
                        );
                        throw new AlreadyLoggedInException("Customer already has an active session");
                    } else {
                        log.info("Expired session found, deleting | email={} | expiredAt={}",
                                dto.getEmail(),
                                session.getExpiryTime()
                        );
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

        log.info(
                "Login successful | email={} | customerId={} | tokenExpiresIn={}s",
                dto.getEmail(),
                customer.getCustomerId(),
                session.getExpiryTime()
        );

        return new AuthResponseDTO(
                token,
                "Bearer",
                Duration.between(LocalDateTime.now(), expiryTime).getSeconds()
        );
    }

    @Override
    @Cacheable(
            value = "customerProfile",
            key = "#email",
            unless = "#result == null"
    )
    public CustomerResponseDTO viewProfile(String email) {
        log.info("View profile request | email={}", email);
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("View profile failed | reason=CUSTOMER_NOT_FOUND | email={}", email);
            return new CustomerNotFoundException("Customer not found.");
        });
        log.debug("Customer profile fetched from DB | email={}", email);
        return toResponseDto(customer);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    value = "customerProfile",
                    key = "#email"
            ),
            @CacheEvict(
                    value = "bankCustomers",
                    allEntries = true
            )
    })

    public CustomerResponseDTO updateProfile(String email, CustomerUpdateDTO updateDTO) {
        log.info("Update profile request | email={}", email);
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Update profile failed | reason=CUSTOMER_NOT_FOUND | email={}", email);
            return new CustomerNotFoundException("Customer not found.");
        });
        if(customerRepository.existsByPhone(updateDTO.getPhone())){
            log.warn("Update profile failed | reason=PHONE_ALREADY_EXISTS | email={}", email);
            throw new CustomerAlreadyExistsException("Phone number already exists!");
        }
        customer.setName(updateDTO.getName());
        customer.setPhone(updateDTO.getPhone());
        customer.setAddress(updateDTO.getAddress());
        customerRepository.save(customer);
        log.info(
                "Profile updated successfully | customerId={} | email={} | cacheEvicted=[customerProfile, bankCustomers]",
                customer.getCustomerId(),
                email
        );
        return toResponseDto(customer);
    }

    @Cacheable(
            value = "bankCustomers",
            key = "{#bankId, #minBalance, #maxBalance, #page, #size}",
            unless = "#result == null"
    )
    @Override
    public PageResponse<CustomerBalanceDTO> fetchCustomersByBankIdAndBalance(
            Long bankId,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            int page,
            int size) {

        log.info(
                "Fetch customers by balance request | bankId={} | minBalance={} | maxBalance={} | page={} | size={}",
                bankId,
                minBalance,
                maxBalance,
                page,
                size
        );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Object[]> results = customerRepository
                .findCustomersByBankIdAndBalance(bankId, minBalance, maxBalance,pageable);

        if (results.isEmpty()) {
            log.warn(
                    "No customers found | bankId={} | minBalance={} | maxBalance={}",
                    bankId,
                    minBalance,
                    maxBalance
            );
            throw new CustomerNotFoundException("No customers found");
        }
        log.debug(
                "Customers fetched from DB | bankId={} | totalElements={} | totalPages={}",
                bankId,
                results.getTotalElements(),
                results.getTotalPages()
        );

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

        log.info(
                "Fetch customers by balance successful | bankId={} | returnedCount={} | page={} | isLast={}",
                bankId,
                data.size(),
                results.getNumber(),
                results.isLast()
        );

        return new PageResponse<>(
                data,
                results.getNumber(),
                results.getTotalPages(),
                results.getTotalElements(),
                results.isLast()
        );
    }
}
