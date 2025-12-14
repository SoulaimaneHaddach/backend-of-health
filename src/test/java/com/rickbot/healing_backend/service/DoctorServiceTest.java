package com.rickbot.healing_backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.entity.Location;
import com.rickbot.healing_backend.mapper.DoctorMapper;
import com.rickbot.healing_backend.repository.DoctorRepo;
import com.rickbot.healing_backend.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepo doctorRepo;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private DoctorRequestDto requestDto;
    private DoctorResponseDto responseDto;
    private Location location;
    private UUID doctorId;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();

        location = new Location();
        location.setLatitude(33.5731);
        location.setLongitude(-7.5898);
        location.setAddress("Test Address");

        doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setName("Dr. Test");
        doctor.setEmail("test@example.com");
        doctor.setSpecialization("General");
        doctor.setLocation(location);
        doctor.setRating(4.5);
        doctor.setReviewsCount(100);

        requestDto = new DoctorRequestDto();
        requestDto.setName("Dr. Test Updated");
        requestDto.setEmail("test@example.com");
        requestDto.setSpecialization("Specialist");
        requestDto.setLocation(location);

        responseDto = new DoctorResponseDto();
        responseDto.setId(doctorId);
        responseDto.setName("Dr. Test");
        responseDto.setEmail("test@example.com");
        responseDto.setSpecialization("General");
        responseDto.setLocation(location);
    }

    @Test
    void findDoctors_WithNoSpecialization_ShouldReturnAllDoctors() {
        // Arrange
        List<Doctor> doctors = Arrays.asList(doctor);
        when(doctorRepo.findAll()).thenReturn(doctors);
        when(doctorMapper.toDto(any(Doctor.class))).thenReturn(responseDto);

        // Act
    List<DoctorResponseDto> result = doctorService.findDoctors( 33.5731, -7.5898, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        
        // Verify
        verify(doctorRepo).findAll();
        verify(doctorMapper).toDto(doctor);
    }

    @Test
    void updateDoctorDetails_WhenDoctorExists_ShouldUpdateDoctor() {
        // Arrange
        when(doctorRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorRepo.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toDto(any(Doctor.class))).thenReturn(responseDto);
        
        // Act
        DoctorResponseDto result = doctorService.updateDoctorDetails(doctorId, requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(doctorId, result.getId());

        // Verify
        verify(doctorRepo).findById(doctorId);
        verify(doctorMapper).updateEntityFromDto(requestDto, doctor);
        verify(doctorRepo).save(doctor);
        verify(doctorMapper).toDto(doctor);
    }

    @Test
    void updateDoctorDetails_WhenDoctorDoesNotExist_ShouldThrowException() {
        // Arrange
        when(doctorRepo.findById(doctorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
            () -> doctorService.updateDoctorDetails(doctorId, requestDto),
            "Doctor not found with id: " + doctorId);

        // Verify
        verify(doctorRepo).findById(doctorId);
        verify(doctorRepo, never()).save(any());
    }

    @Test
    void verifyJwtToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = "valid_token";
        when(jwtUtil.validateToken(token)).thenReturn(true);

        // Act
        boolean result = doctorService.verifyJwtToken(token);

        // Assert
        assertTrue(result);

        // Verify
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void verifyJwtToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String token = "invalid_token";
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Act
        boolean result = doctorService.verifyJwtToken(token);

        // Assert
        assertFalse(result);

        // Verify
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void getDoctorProfile_WhenDoctorExists_ShouldReturnDoctorProfile() {
        // Arrange
        when(doctorRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toDto(doctor)).thenReturn(responseDto);

        // Act
        DoctorResponseDto result = doctorService.getDoctorProfile(doctorId);

        // Assert
        assertNotNull(result);
        assertEquals(doctorId, result.getId());
        assertEquals(doctor.getName(), result.getName());
        assertEquals(doctor.getEmail(), result.getEmail());

        // Verify
        verify(doctorRepo).findById(doctorId);
        verify(doctorMapper).toDto(doctor);
    }

    @Test
    void getDoctorProfile_WhenDoctorDoesNotExist_ShouldThrowException() {
        // Arrange
        when(doctorRepo.findById(doctorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
            () -> doctorService.getDoctorProfile(doctorId),
            "Doctor not found");

        // Verify
        verify(doctorRepo).findById(doctorId);
        verify(doctorMapper, never()).toDto(any());
    }
}