package com.AuthSecurity.AuthenticationApp.Dtos;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String message,
        HttpStatus status,
        String path,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of( String message,
                                    HttpStatus status,
                                    String path,
                                    OffsetDateTime timestamp){
        return new ErrorResponse(message,status,path,timestamp);
    }
}
