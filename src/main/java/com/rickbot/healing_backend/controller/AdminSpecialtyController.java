package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.dto.AdminSpecialtyRequest;
import com.rickbot.healing_backend.dto.AdminSpecialtyResponse;
import com.rickbot.healing_backend.service.SpecialtyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/specialties")
public class AdminSpecialtyController {
    private final SpecialtyService specialtyService;

    public AdminSpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminSpecialtyResponse>> list() {
        return ResponseEntity.ok(specialtyService.getAll());
    }

    @GetMapping("/ping")
    // intentionally public for quick debug checks; security config permits this path
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSpecialtyResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(specialtyService.findById(UUID.fromString(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSpecialtyResponse> create(@RequestBody AdminSpecialtyRequest req) {
        return ResponseEntity.status(201).body(specialtyService.create(req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSpecialtyResponse> update(@PathVariable String id, @RequestBody AdminSpecialtyRequest req) {
        return ResponseEntity.ok(specialtyService.update(UUID.fromString(id), req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        specialtyService.delete(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }
}
