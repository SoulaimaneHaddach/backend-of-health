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
import com.rickbot.healing_backend.dto.DoctorRegisterRequestDTO;
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

    private DoctorRegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private Doctor doctor;
    private DoctorPrincipal doctorPrincipal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup register request
        registerRequest = new DoctorRegisterRequestDTO(
            "Dr. Test",
            "test@example.com",
            "password123",
            "General",
            "1234567890",
            33.5731,
            -7.5898,
            "Test Address"
        );

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

    @Test
    void register_WhenEmailNotTaken_ShouldRegisterSuccessfully() {
        // Arrange
        when(doctorRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(doctorRepository.save(any())).thenReturn(doctor);
        when(jwtUtil.generateToken(any())).thenReturn("token");
        when(jwtUtil.getJwtExpirationMs()).thenReturn(3600000L);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Bearer", response.tokenType());
        assertEquals("token", response.accessToken());
        assertEquals(3600000L, response.expiresInMs());
        assertEquals("test@example.com", response.email());

        // Verify
        verify(doctorRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(doctorRepository).save(any(Doctor.class));
        verify(jwtUtil).generateToken(any(DoctorPrincipal.class));
    }

    @Test
    void register_WhenEmailTaken_ShouldThrowException() {
        // Arrange
        when(doctorRepository.findByEmail(any())).thenReturn(Optional.of(doctor));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> authService.register(registerRequest),
            "Email already registered");

        // Verify
        verify(doctorRepository).findByEmail("test@example.com");
        verify(doctorRepository, never()).save(any());
    }

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