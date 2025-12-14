package com.rickbot.healing_backend.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;

    // Endpoint to verify a doctor
    @PutMapping("/api/admin/verify")
    public void verifyDoctor(@RequestParam UUID doctorId) {
        adminService.verifyDoctor(doctorId);
    }

    // Endpoint to unverify a doctor
    @PutMapping("/api/admin/unverify")
    public void unverifyDoctor(@RequestParam UUID doctorId) {
        adminService.unverifyDoctor(doctorId);
    }
}
