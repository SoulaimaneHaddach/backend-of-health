package com.rickbot.healing_backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.mapper.DoctorMapper;
import com.rickbot.healing_backend.service.DoctorService;

import lombok.Data;


@RestController     @Data
public class DoctorController {
    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    @GetMapping("/api/doctors")
    public List<DoctorResponseDto> getDoctors(@RequestParam(required = false) String specialization
                                            // @RequestParam(required = false) Double latitude,
                                            // @RequestParam(required = false) Double longitude
                                            ) {
    if (specialization == null) {
        return doctorService.findDoctors(null);
    }
    return doctorService.findDoctors(specialization//,latitude,longitude
    );
}


    //update doctor details
    @PutMapping("/api/doctors/{id}")
    public ResponseEntity<DoctorResponseDto> updateDoctor(
            @PathVariable UUID id,
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
    @GetMapping("/api/doctors/me")
    public ResponseEntity<DoctorResponseDto> getMyProfile(Principal principal) {
        Doctor doctor = doctorService.findByEmail(principal.getName());
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        DoctorResponseDto dto = doctorMapper.toDto(doctor);
        return ResponseEntity.ok(dto);
}



    // Update current doctor profile (including location)
    @PutMapping("/api/doctors/me")
    public ResponseEntity<DoctorResponseDto> updateMyProfile(
            Principal principal,
            @RequestBody DoctorRequestDto requestDto) {
        String email = principal.getName(); // get logged-in user's email
        Doctor doctor = doctorService.findByEmail(email);
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        DoctorResponseDto updated = doctorService.updateDoctor(doctor.getId(), requestDto);
        return ResponseEntity.ok(updated);
}


    

}
