package com.ritik.customer_microservice.feign;

import com.ritik.customer_microservice.dto.external.BankResponseDTO;
import com.ritik.customer_microservice.serviceImpl.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "bank-microservice")
public interface BankClient {

    @GetMapping("/api/banks")
    PageResponse<BankResponseDTO> getBanks(@RequestParam(required = false) String ifsc,
                                           @RequestParam(required = false, name = "bank_id") Long bankId);
}
