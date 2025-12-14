package com.rickbot.healing_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportsController {

    // Minimal stubs: frontend expects these endpoints. Implementations return empty data structures
    // to avoid breaking the UI; real implementation can be added later.

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingStats(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        // Return an empty list or simple structure that the frontend can handle.
        List<Object> empty = List.of();
        return ResponseEntity.ok(Map.of("data", empty));
    }

    @GetMapping("/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDoctorStats() {
        List<Object> empty = List.of();
        return ResponseEntity.ok(Map.of("data", empty));
    }
}
