package com.ritik.bank_microservice.service;


import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;
import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import com.ritik.bank_microservice.wrapper.PageResponse;

import java.math.BigDecimal;

public interface BankService {

    BankResponseDTO addBank(BankRequestDTO dto);

    PageResponse<BankResponseDTO> getBankDetails(String ifsc, Long id, int page, int size);

    PageResponse<CustomerBalanceDTO> getCustomersByIfsc(
            String ifsc,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            int page,
            int size
    );
}
