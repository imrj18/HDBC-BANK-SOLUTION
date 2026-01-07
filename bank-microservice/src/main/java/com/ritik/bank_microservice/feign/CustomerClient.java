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
    PageResponse<CustomerBalanceDTO> getCustomers(@RequestParam Long bankId,
                                                  @RequestParam(required = false) BigDecimal minBalance,
                                                  @RequestParam(required = false) BigDecimal maxBalance,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "5") int size);
}

