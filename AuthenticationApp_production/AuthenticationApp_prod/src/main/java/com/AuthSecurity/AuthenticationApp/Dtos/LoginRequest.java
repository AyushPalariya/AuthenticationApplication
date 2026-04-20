package com.AuthSecurity.AuthenticationApp.Dtos;

public record LoginRequest(
        String identifier,
        String password
) {
}
