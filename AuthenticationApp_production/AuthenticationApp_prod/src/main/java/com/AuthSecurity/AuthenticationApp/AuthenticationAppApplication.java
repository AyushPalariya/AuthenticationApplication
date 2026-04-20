package com.AuthSecurity.AuthenticationApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AuthenticationAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthenticationAppApplication.class, args);
    }
}
