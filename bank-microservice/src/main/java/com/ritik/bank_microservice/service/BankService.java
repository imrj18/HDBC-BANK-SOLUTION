package com.ritik.bank_microservice.service;


import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.dto.CustomerBalanceDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BankService {

    BankResponseDTO addBank(BankRequestDTO dto);

    List<BankResponseDTO> getBankDetails(String ifsc, Long id);

    List<CustomerBalanceDTO> getCustomersByIfsc(String ifsc, BigDecimal minBalance, BigDecimal maxBalance);
}
