package com.ritik.customer_microservice.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRegisterDTO {


    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 100, message = "Address must be between 10 and 100 characters")
    private String address;

    @NotBlank(message = "Aadhar is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhar must be 12 digits")
    private String aadhar;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

}
