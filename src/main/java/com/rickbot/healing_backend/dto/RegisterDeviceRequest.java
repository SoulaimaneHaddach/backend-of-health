package com.rickbot.healing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDeviceRequest {
    private String token;
    private String platform;
    // userId MUST NOT be supplied by clients; server will use authenticated user id
}
