package com.rickbot.healing_backend.service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.dto.AuthResponse;
import com.rickbot.healing_backend.dto.DoctorRegisterRequestDTO;
import com.rickbot.healing_backend.dto.LoginRequestDTO;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.entity.Location;
import com.rickbot.healing_backend.repository.DoctorRepo;
import com.rickbot.healing_backend.security.DoctorPrincipal;
import com.rickbot.healing_backend.security.DoctorServiceDetails;
import com.rickbot.healing_backend.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final DoctorRepo doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final DoctorServiceDetails doctorDetailsService;

    public AuthResponse register(DoctorRegisterRequestDTO r) {
        if (doctorRepository.findByEmail(r.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        Doctor doc = new Doctor();
        doc.setName(r.name());
        doc.setSpecialization(r.specialization());
        doc.setEmail(r.email());
        doc.setPassword(passwordEncoder.encode(r.password()));
        doc.setPhoneNumber(r.phoneNumber());

        Location loc = new Location();
        loc.setLatitude(r.latitude());
        loc.setLongitude(r.longitude());
        loc.setAddress(r.address());
        doc.setLocation(loc);

        doctorRepository.save(doc);

        DoctorPrincipal principal = new DoctorPrincipal(doc);
        String token = jwtUtil.generateToken(principal);

        return new AuthResponse(token, "Bearer", jwtUtil.getJwtExpirationMs(), doc.getEmail());
    }

    public AuthResponse login(LoginRequestDTO req) {
        // authenticate (will call CustomDoctorDetailsService to load user)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        DoctorPrincipal principal = (DoctorPrincipal) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal);

        return new AuthResponse(token, "Bearer", jwtUtil.getJwtExpirationMs(), principal.getUsername());
    }
}



