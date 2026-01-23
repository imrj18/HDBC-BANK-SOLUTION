package com.ritik.bank_microservice;

import com.ritik.bank_microservice.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(defaultConfiguration = FeignConfig.class)
@EnableCaching
@SpringBootApplication
public class BankMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankMicroserviceApplication.class, args);
	}

}
