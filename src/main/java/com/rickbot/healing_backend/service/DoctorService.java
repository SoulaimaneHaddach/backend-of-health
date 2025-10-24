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
        public List<DoctorResponseDto> findDoctors(String specialization //,double latitude,double longitude
        ) {
        return doctorRepo.findAll().stream()
                // Filter by specialization if provided
                .filter(d -> specialization == null
                        || specialization.isEmpty()
                        || specialization.equalsIgnoreCase("All")
                        || (d.getSpecialization() != null
                                && d.getSpecialization().equalsIgnoreCase(specialization)))
                // Keep only doctors with a near location to latitude and longtitude
                .filter(d -> d.getLocation() != null
                        //&& isWithinRadius(d.getLocation().getLatitude(), d.getLocation().getLongitude(), latitude, longitude, 50)
                        ) // 50 km radius
                // Map to DTO
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
        }





        private boolean isWithinRadius(double latitude, double longitude, double latitude2, double longitude2, int i) {
                // verify if location 1(latitude,longtitude) is near too location 2(latitude2,longtitude2) by i km
                final int EARTH_RADIUS_KM = 6371;
                double latDiff = Math.toRadians(latitude2 - latitude);
                double lonDiff = Math.toRadians(longitude2 - longitude);

                double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(latitude2))
                                * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);

                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double distanceKm = EARTH_RADIUS_KM * c;

                return distanceKm <= i;
                

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
