package com.rickbot.healing_backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.dto.DoctorMapDto;
import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.mapper.DoctorMapper;
import com.rickbot.healing_backend.repository.DoctorRepo;
import com.rickbot.healing_backend.security.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@Data
@RequiredArgsConstructor
public class DoctorService {

    private final PasswordEncoder passwordEncoder;
    private final DoctorRepo doctorRepo;
    private final DoctorMapper doctorMapper;
    private final JwtUtil jwtUtil;

    @Value("${app.doctors.max-results:1000}")
    private int maxDoctorResults;

    /**
     * Find doctors with optional radius filtering. Caches results in Redis.
     */
    @Cacheable(
        value = "doctorsCache",
        key = "#latitude + '-' + #longitude + '-' + #radiusKm",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<DoctorResponseDto> findDoctors(Double latitude, Double longitude, Double radiusKm) {
        final double effectiveRadiusMeters = (radiusKm != null ? radiusKm : 50d) * 1000d; // default 50 km
        // If coordinates provided, perform DB-side radius query (more efficient than fetching entire table)
        if (latitude != null && longitude != null) {
            org.springframework.data.domain.Pageable page = PageRequest.of(0, Math.max(1, maxDoctorResults));
            return doctorRepo.findDoctorsWithinRadius(latitude, longitude, effectiveRadiusMeters, page).stream()
                    .map(doctorMapper::toDto)
                    .collect(Collectors.toList());
        }

        // Fallback: fetch a limited page and filter in-memory
        return doctorRepo.findAll(PageRequest.of(0, Math.max(1, maxDoctorResults))).stream()
                .filter(d -> d.getLocation() != null)
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors for map display with distance sorting.
     */
    public List<DoctorMapDto> findDoctorsForMap(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("latitude and longitude required");
        }

        // default 20 km, clamp to [1,50]
        double requested = (radiusKm == null ? 20d : radiusKm);
        double clampedKm = Math.min(50d, Math.max(1d, requested));
        final double effectiveRadiusMeters = clampedKm * 1000d;

        // Use DB-side radius query to limit results and then sort in-memory by distance
        org.springframework.data.domain.Pageable page = PageRequest.of(0, Math.max(1, maxDoctorResults));
        List<Doctor> doctors = doctorRepo.findDoctorsWithinRadius(latitude, longitude, effectiveRadiusMeters, page);

        return doctors.stream()
            .map(doctorMapper::toMapDto)
            .sorted(Comparator.comparingDouble(d -> distanceMeters(
                d.getLocation().getLatitude(),
                d.getLocation().getLongitude(),
                latitude,
                longitude)))
            .collect(Collectors.toList());
    }

    /**
     * Haversine formula to check if two coordinates are within a radius in meters.
     */
    private boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusMeters) {
        final double EARTH_RADIUS_METERS = 6_371_000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double sinDLat = Math.sin(dLat / 2);
        double sinDLon = Math.sin(dLon / 2);

        double a = sinDLat * sinDLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * sinDLon * sinDLon;

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceMeters = EARTH_RADIUS_METERS * c;

        return distanceMeters <= radiusMeters;
    }

    /**
     * Haversine formula to calculate distance between two coordinates.
     */
    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_METERS = 6_371_000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double sinDLat = Math.sin(dLat / 2);
        double sinDLon = Math.sin(dLon / 2);

        double a = sinDLat * sinDLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * sinDLon * sinDLon;

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Update doctor details
     */
    @Transactional
    public DoctorResponseDto updateDoctorDetails(UUID id, DoctorRequestDto doctorDto) {
        Doctor doctor = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        doctorMapper.updateEntityFromDto(doctorDto, doctor);

        if (doctorDto.getPassword() != null && !doctorDto.getPassword().isEmpty()) {
            doctor.setPassword(passwordEncoder.encode(doctorDto.getPassword()));
        }

        Doctor updatedDoctor = doctorRepo.save(doctor);
        return doctorMapper.toDto(updatedDoctor);
    }

    /**
     * Verify JWT token
     */
    public boolean verifyJwtToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    public DoctorResponseDto updateDoctor(UUID id, DoctorRequestDto dto) {
        Doctor doctor = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctorMapper.updateDoctorFromDto(dto, doctor);  // mapper handles all fields
        doctor = doctorRepo.save(doctor);

        return doctorMapper.toDto(doctor);
    }

    public DoctorResponseDto getDoctorProfile(UUID id) {
        Doctor doctor = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return doctorMapper.toDto(doctor);
    }

    public Doctor findByEmail(String email) {
        return doctorRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + email));
    }
}
