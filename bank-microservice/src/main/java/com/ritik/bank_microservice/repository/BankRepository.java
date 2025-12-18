package com.ritik.bank_microservice.repository;


import com.ritik.bank_microservice.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank,Long> {
    Optional<Bank> findByIfscCode(String ifscCode);

}

