package com.rickbot.healing_backend.service;

import java.util.List;
import java.util.UUID;
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
        public List<DoctorResponseDto> findDoctors(String speciality,double lat, double lng){
            return doctorRepo.findBySpecializationIgnoreCase(speciality).stream()
                    .filter(doctor -> {
                        double distance = Math.sqrt(Math.pow(doctor.getLocation().getLatitude() - lat, 2) +
                                Math.pow(doctor.getLocation().getLongitude() - lng, 2));
                        return distance <= 10; // Filter doctors within 10 units distance
                    })
                    .map(doctorMapper::toDto)
                    .toList();
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


}
