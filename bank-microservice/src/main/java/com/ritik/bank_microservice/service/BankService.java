package com.ritik.bank_microservice.service;


import com.ritik.bank_microservice.dto.BankRequestDTO;
import com.ritik.bank_microservice.dto.BankResponseDTO;

import java.util.List;

public interface BankService {

    BankResponseDTO addBank(BankRequestDTO dto);

    //List<Bank> getAllBank();

    //Bank getBankByIFSC(String ifsc);

    //Bank getBankById(long id);

    List<BankResponseDTO> getBankDetails(String ifsc, Long id);
}
