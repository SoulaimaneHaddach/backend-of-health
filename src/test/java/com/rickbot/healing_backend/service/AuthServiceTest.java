package com.rickbot.healing_backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rickbot.healing_backend.dto.AuthResponse;
// DoctorRegisterRequestDTO removed; registration tests removed accordingly
import com.rickbot.healing_backend.dto.LoginRequestDTO;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.repository.DoctorRepo;
import com.rickbot.healing_backend.security.DoctorPrincipal;
import com.rickbot.healing_backend.security.DoctorServiceDetails;
import com.rickbot.healing_backend.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private DoctorRepo doctorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private DoctorServiceDetails doctorDetailsService;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDTO loginRequest;
    private Doctor doctor;
    private DoctorPrincipal doctorPrincipal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup login request
        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Setup doctor
        doctor = new Doctor();
        doctor.setEmail("test@example.com");
        doctor.setPassword("encodedPassword");
        doctor.setName("Dr. Test");

        // Setup doctor principal
        doctorPrincipal = new DoctorPrincipal(doctor);

        // Setup authentication
        authentication = mock(Authentication.class);
    }

    // Registration tests removed because registration is not supported by the API

    @Test
    void login_WhenCredentialsValid_ShouldLoginSuccessfully() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(doctorPrincipal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtil.generateToken(any(DoctorPrincipal.class))).thenReturn("token");
        when(jwtUtil.getJwtExpirationMs()).thenReturn(3600000L);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Bearer", response.tokenType());
        assertEquals("token", response.accessToken());
        assertEquals(3600000L, response.expiresInMs());
        assertEquals("test@example.com", response.email());

        // Verify
        verify(authenticationManager).authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        verify(jwtUtil).generateToken(doctorPrincipal);
    }
}