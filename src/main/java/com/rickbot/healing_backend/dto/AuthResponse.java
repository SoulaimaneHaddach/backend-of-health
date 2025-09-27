package com.rickbot.healing_backend.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInMs,
    String email
) {
    
}
