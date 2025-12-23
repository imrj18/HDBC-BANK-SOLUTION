package com.ritik.customer_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CustomerMicroserviceApplication {

	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		String raw = "R@123";
		String hash = "$2a$12$WlLSd1kkwie9tgE8To7gO.SBUZFrG7PRI3B6T0aWENr92wPdhTJ56";

		System.out.println(encoder.matches(raw, hash));
		SpringApplication.run(CustomerMicroserviceApplication.class, args);
	}

}
