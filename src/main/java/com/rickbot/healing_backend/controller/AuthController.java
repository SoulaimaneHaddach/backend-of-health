package com.rickbot.healing_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.dto.AuthResponse;
import com.rickbot.healing_backend.dto.DoctorRegisterRequestDTO;
import com.rickbot.healing_backend.dto.LoginRequestDTO;
import com.rickbot.healing_backend.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid DoctorRegisterRequestDTO req) {
        AuthResponse resp = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO req) {
        try {
            AuthResponse resp = authService.login(req);
            return ResponseEntity.ok(resp);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Invalid email or password"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", ex.getMessage()));
        }
    }
    // logout
}
