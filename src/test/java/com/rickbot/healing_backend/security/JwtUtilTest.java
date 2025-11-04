package com.rickbot.healing_backend.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.rickbot.healing_backend.entity.Doctor;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private Doctor doctor;
    private DoctorPrincipal doctorPrincipal;

    @BeforeEach
    void setUp() {
        // Initialize JwtUtil with test secret and expiration
        String testSecret = "ZmM4ZDQ4YzI5ZTg5ZDA2MTU2Y2JlZjYxMzIyZTg2NzY4MTZkNDEyNmY2YjA1ZjU1YzM3NzQ0ZjMwZDQwMjk1Nw==";
        long testExpiration = 3600000; // 1 hour
        jwtUtil = new JwtUtil(testSecret, testExpiration);

        // Create a test doctor
        doctor = new Doctor();
        doctor.setId(UUID.randomUUID());
        doctor.setEmail("test@example.com");
        doctor.setName("Dr. Test");

        // Create doctor principal
        doctorPrincipal = new DoctorPrincipal(doctor);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Act
        String token = jwtUtil.generateToken(doctorPrincipal);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtil.generateToken(doctorPrincipal);

        // Act & Assert
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        // Arrange
        String token = jwtUtil.generateToken(doctorPrincipal);

        // Act
        String email = jwtUtil.getEmailFromToken(token);

        // Assert
        assertEquals(doctor.getEmail(), email);
    }

    @Test
    void generateToken_ShouldIncludeCorrectClaims() {
        // Arrange
        doctorPrincipal = new DoctorPrincipal(doctor) {
            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR"));
            }
        };

        // Act
        String token = jwtUtil.generateToken(doctorPrincipal);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(doctor.getEmail(), jwtUtil.getEmailFromToken(token));
    }

    @Test
    void getJwtExpirationMs_ShouldReturnConfiguredValue() {
        // Act
        long expirationMs = jwtUtil.getJwtExpirationMs();

        // Assert
        assertEquals(3600000, expirationMs);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        JwtUtil shortExpirationJwtUtil = new JwtUtil(
            "ZmM4ZDQ4YzI5ZTg5ZDA2MTU2Y2JlZjYxMzIyZTg2NzY4MTZkNDEyNmY2YjA1ZjU1YzM3NzQ0ZjMwZDQwMjk1Nw==",
            1 // 1ms expiration
        );
        String token = shortExpirationJwtUtil.generateToken(doctorPrincipal);

        try {
            // Wait for token to expire
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertFalse(shortExpirationJwtUtil.validateToken(token));
    }
}