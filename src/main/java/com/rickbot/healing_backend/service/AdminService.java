package com.rickbot.healing_backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.repository.DoctorRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final DoctorRepo doctorRepo;
    public void verifyDoctor(UUID doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        doctor.setVerified(true);
        doctorRepo.save(doctor);
    }

    public void unverifyDoctor(UUID doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        doctor.setVerified(false);
        doctorRepo.save(doctor);
    }
}
