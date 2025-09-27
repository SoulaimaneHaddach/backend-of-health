package com.rickbot.healing_backend.dto;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInMs,
    String email,
    UUID id
) {
    
}
