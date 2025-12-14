package com.rickbot.healing_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.dto.AdminLoginRequest;
import com.rickbot.healing_backend.security.JwtUtil;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final JwtUtil jwtUtil;
    private final com.rickbot.healing_backend.service.AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "email and password required"));
        }

        var adminOpt = adminService.findEntityByEmail(req.getEmail());
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }

        var admin = adminOpt.get();
        org.springframework.security.crypto.password.PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!encoder.matches(req.getPassword(), admin.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }
        if (!admin.isEnabled()) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin disabled"));
        }

        String token = jwtUtil.generateToken(admin.getEmail(), java.util.List.copyOf(admin.getRoles()));

        return ResponseEntity.ok(Map.of(
            "token", token,
            "accessToken", token,
            "adminToken", token
        ));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> loginAuth(@Valid @RequestBody AdminLoginRequest req) {
        // Alias for `/login` used by the admin frontend (`/admin/auth/login`)
        return login(req);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        var adminOpt = adminService.findEntityByEmail(principal.getName());
        if (adminOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("message", "Admin not found"));
        var a = adminOpt.get();
        return ResponseEntity.ok(Map.of(
            "id", a.getId().toString(),
            "name", a.getName(),
            "email", a.getEmail(),
            "roles", a.getRoles()
        ));
    }
}
