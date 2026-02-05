package com.rickbot.healing_backend.service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.dto.AuthResponse;
import com.rickbot.healing_backend.dto.LoginRequestDTO;
import com.rickbot.healing_backend.security.DoctorPrincipal;
import com.rickbot.healing_backend.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequestDTO req) {
        // authenticate (will call CustomDoctorDetailsService to load user)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        DoctorPrincipal principal = (DoctorPrincipal) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal);

        return new AuthResponse(token, "Bearer", jwtUtil.getJwtExpirationMs(), principal.getUsername(),principal.getId());
    }
}



