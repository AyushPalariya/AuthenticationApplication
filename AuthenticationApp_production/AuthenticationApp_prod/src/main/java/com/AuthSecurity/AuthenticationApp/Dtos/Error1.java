package com.AuthSecurity.AuthenticationApp.Dtos;

import org.springframework.http.HttpStatus;

public record Error1(
        String message,
        HttpStatus status
) {
}
