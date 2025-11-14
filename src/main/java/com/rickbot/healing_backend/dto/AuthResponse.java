package com.rickbot.healing_backend.dto;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInMs,
    String email,
    String id  // Changed from UUID to String for Android compatibility
) {
    // Constructor to convert UUID to String
    public AuthResponse(String accessToken, String tokenType, long expiresInMs, String email, UUID id) {
        this(accessToken, tokenType, expiresInMs, email, id != null ? id.toString() : null);
    }
}
