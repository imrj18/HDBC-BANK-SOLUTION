package com.ritik.bank_microservice.feign;

import com.ritik.bank_microservice.dto.CustomerBalanceDTO;
import com.ritik.bank_microservice.wrapper.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(
        name = "customer-microservice",
        configuration = FeignConfig.class
)
public interface CustomerClient {

    @GetMapping("/internal/customers")
    PageResponse<CustomerBalanceDTO> getCustomers(
            @RequestParam("bankId") Long bankId,
            @RequestParam(value = "minBalance", required = false) BigDecimal minBalance,
            @RequestParam(value = "maxBalance", required = false) BigDecimal maxBalance,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size
    );
}

