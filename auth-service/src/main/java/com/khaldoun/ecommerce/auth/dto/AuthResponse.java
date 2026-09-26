package com.khaldoun.ecommerce.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user) {

    public AuthResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }

    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}