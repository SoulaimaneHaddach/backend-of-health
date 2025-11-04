package com.rickbot.healing_backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.mapper.DoctorMapper;
import com.rickbot.healing_backend.repository.DoctorRepo;

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
        private final com.rickbot.healing_backend.security.JwtUtil jwtUtil;

        @Transactional
        public List<DoctorResponseDto> findDoctors(String specialization ,double latitude,double longitude) {
        return doctorRepo.findAll().stream()
                // Filter by specialization if provided
                .filter(d -> specialization == null
                        || specialization.isEmpty()
                        || specialization.equalsIgnoreCase("All")
                        || (d.getSpecialization() != null
                                && d.getSpecialization().equalsIgnoreCase(specialization)))
                // Keep only doctors with a near location to latitude and longitude
                .filter(d -> d.getLocation() != null
                        && isWithinRadius(d.getLocation().getLatitude(), d.getLocation().getLongitude(), latitude, longitude, 1500)) // 50 km radius
                // Map to DTO
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
        }
        





        private boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusMeters) {
                // Haversine formula with meters for higher precision
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





        //update doctor details
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
        Doctor doctor = doctorRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + email));
        return doctor;
    }

}
