package com.ritik.bank_microservice.feign;

import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(
        name = "customer-microservice",
        configuration = FeignConfig.class
)
public interface CustomerClient {

    @GetMapping("/internal/customers")
    List<CustomerBalanceDTO> getCustomers(@RequestParam Long bankId,
                                          @RequestParam(required = false) BigDecimal minBalance,
                                          @RequestParam(required = false) BigDecimal maxBalance);
}

