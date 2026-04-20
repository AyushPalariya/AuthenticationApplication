package com.AuthSecurity.AuthenticationApp.Dtos;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expireTime,
        UserDTO user
) {
    public static TokenResponse of( String accessToken,
                                    String refreshToken,
                                    long expireTime,
                                    UserDTO user){
        return new TokenResponse(accessToken, refreshToken, "Bearer", expireTime, user);
    }
}
