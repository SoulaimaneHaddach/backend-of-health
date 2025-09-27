package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.dto.AuthResponse;
import com.rickbot.healing_backend.dto.DoctorRegisterRequestDTO;
import com.rickbot.healing_backend.dto.LoginRequestDTO;
import com.rickbot.healing_backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    public AuthControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_returnsCreated() {
        DoctorRegisterRequestDTO req = new DoctorRegisterRequestDTO(
                "John Doe", "john@example.com", "pass123", "Cardiology", "1234567890", 1.0, 2.0, "123 St");
        AuthResponse resp = new AuthResponse("token", "Bearer", 3600L, "john@example.com");
        when(authService.register(req)).thenReturn(resp);
        ResponseEntity<AuthResponse> response = authController.register(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        DoctorRegisterRequestDTO req = new DoctorRegisterRequestDTO(
                "Jane Doe", "jane@example.com", "pass123", "Cardiology", "1234567890", 1.0, 2.0, "123 St");
        when(authService.register(req)).thenThrow(new IllegalArgumentException("Email already registered"));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> authController.register(req));
        assertTrue(exception.getMessage().contains("Email already registered"));
    }

    @Test
    void login_returnsOk() {
        LoginRequestDTO req = new LoginRequestDTO("john@example.com", "pass123");
        AuthResponse resp = new AuthResponse("token", "Bearer", 3600L, "john@example.com");
        when(authService.login(req)).thenReturn(resp);
        ResponseEntity<AuthResponse> response = authController.login(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    void login_invalidCredentials_throwsException() {
        LoginRequestDTO req = new LoginRequestDTO("baduser@example.com", "wrongpass");
        when(authService.login(req)).thenThrow(new RuntimeException("Bad credentials"));
        Exception exception = assertThrows(RuntimeException.class, () -> authController.login(req));
        assertTrue(exception.getMessage().contains("Bad credentials"));
    }
}
