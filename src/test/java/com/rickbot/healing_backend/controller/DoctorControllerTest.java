package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorControllerTest {
    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    public DoctorControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDoctors_returnsList() {
        String specialty = "Cardiology";
        double lat = 1.0, lng = 2.0;
        List<DoctorResponseDto> mockList = Collections.singletonList(new DoctorResponseDto());
        when(doctorService.findDoctors(specialty, lat, lng)).thenReturn(mockList);

        ResponseEntity<List<DoctorResponseDto>> response = doctorController.getDoctors(specialty, lat, lng);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockList, response.getBody());
    }

    @Test
    void getDoctors_returnsEmptyList() {
        String specialty = "Unknown";
        double lat = 0.0, lng = 0.0;
        when(doctorService.findDoctors(specialty, lat, lng)).thenReturn(Collections.emptyList());

        ResponseEntity<List<DoctorResponseDto>> response = doctorController.getDoctors(specialty, lat, lng);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void updateDoctor_returnsOk_withValidJwt() {
        UUID id = UUID.randomUUID();
        DoctorRequestDto dto = new DoctorRequestDto();
        DoctorResponseDto updated = new DoctorResponseDto();
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        when(doctorService.verifyJwtToken(validToken)).thenReturn(true);
        when(doctorService.updateDoctorDetails(id, dto)).thenReturn(updated);
        ResponseEntity<DoctorResponseDto> response = doctorController.updateDoctor(id, dto, authHeader);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(updated, response.getBody());
    }

    @Test
    void updateDoctor_returnsUnauthorized_withInvalidJwt() {
        UUID id = UUID.randomUUID();
        DoctorRequestDto dto = new DoctorRequestDto();
        String invalidToken = "invalid.jwt.token";
        String authHeader = "Bearer " + invalidToken;
        when(doctorService.verifyJwtToken(invalidToken)).thenReturn(false);
        ResponseEntity<DoctorResponseDto> response = doctorController.updateDoctor(id, dto, authHeader);
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void updateDoctor_returnsUnauthorized_withMissingJwt() {
        UUID id = UUID.randomUUID();
        DoctorRequestDto dto = new DoctorRequestDto();
        ResponseEntity<DoctorResponseDto> response = doctorController.updateDoctor(id, dto, null);
        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void updateDoctor_doctorNotFound_throwsException_withValidJwt() {
        UUID id = UUID.randomUUID();
        DoctorRequestDto dto = new DoctorRequestDto();
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        when(doctorService.verifyJwtToken(validToken)).thenReturn(true);
        when(doctorService.updateDoctorDetails(id, dto)).thenThrow(new RuntimeException("Doctor not found with id: " + id));
        Exception exception = assertThrows(RuntimeException.class, () -> doctorController.updateDoctor(id, dto, authHeader));
        assertTrue(exception.getMessage().contains("Doctor not found"));
    }

    @Test
    void getDoctors_withFarLocation_returnsEmpty() {
        String specialty = "Cardiology";
        double lat = 1000.0, lng = 1000.0;
        when(doctorService.findDoctors(specialty, lat, lng)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DoctorResponseDto>> response = doctorController.getDoctors(specialty, lat, lng);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}
