package com.ritik.customer_microservice.config;

import com.ritik.customer_microservice.config.WithMockCustomerSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomerSecurityContextFactory.class)
public @interface WithMockCustomer {
    String email() default "jd@gmail.com";
    String[] roles() default {"USER"};
}
