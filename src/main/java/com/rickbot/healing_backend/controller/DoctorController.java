package com.rickbot.healing_backend.controller;

import java.util.List;
import java.util.UUID;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.service.DoctorService;

import lombok.Data;


@RestController     @Data
public class DoctorController {
    private final DoctorService doctorService;

    @GetMapping("/api/doctors")
    public ResponseEntity<List<DoctorResponseDto>> getDoctors(@RequestParam String specialty, @RequestParam double lat, @RequestParam double lng) {
        List<DoctorResponseDto> doctors = doctorService.findDoctors(specialty, lat, lng);
        return ResponseEntity.ok(doctors);
    }

    //update doctor details
    @PutMapping("/api/doctors/{id}")
    public ResponseEntity<DoctorResponseDto> updateDoctor(
            @RequestParam UUID id,
            @RequestBody DoctorRequestDto doctorDto,
            @RequestHeader("Authorization") String authHeader) {
        // Extract token from header (expecting format: "Bearer <token>")
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        if (token == null || !doctorService.verifyJwtToken(token)) {
            return ResponseEntity.status(401).build();
        }
        DoctorResponseDto updatedDoctor = doctorService.updateDoctorDetails(id, doctorDto);
        return ResponseEntity.ok(updatedDoctor);
    }
    

}
