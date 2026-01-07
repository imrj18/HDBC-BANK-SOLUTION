package com.ritik.customer_microservice.repository;

import com.ritik.customer_microservice.model.AccountNumberGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountNumberGeneratorRepository extends JpaRepository<AccountNumberGenerator, Long> {

}
