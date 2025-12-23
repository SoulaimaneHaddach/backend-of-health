package com.rickbot.healing_backend.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.dto.AdminDoctorRequest;
import com.rickbot.healing_backend.dto.AdminDoctorResponse;
import com.rickbot.healing_backend.service.DoctorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/doctors")
@Validated
@RequiredArgsConstructor
public class AdminDoctorController {

    private final DoctorService doctorService;

    // Only admins
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AdminDoctorRequest req) {
        try {
            AdminDoctorResponse created = doctorService.createAdminDoctor(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            if ("EMAIL_EXISTS".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "Email already exists"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdminDoctorResponse>> list() {
        List<AdminDoctorResponse> list = doctorService.listAllForAdmin();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable("id") UUID id) {
        try {
            AdminDoctorResponse updated = doctorService.approveDoctor(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Doctor not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable("id") UUID id) {
        try {
            AdminDoctorResponse updated = doctorService.rejectDoctor(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Doctor not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/disable")
    public ResponseEntity<?> disable(@PathVariable("id") UUID id) {
        try {
            AdminDoctorResponse updated = doctorService.disableDoctor(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Doctor not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/enable")
    public ResponseEntity<?> enable(@PathVariable("id") UUID id) {
        try {
            AdminDoctorResponse updated = doctorService.enableDoctor(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Doctor not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @Valid @RequestBody AdminDoctorRequest req) {
        try {
            com.rickbot.healing_backend.dto.DoctorRequestDto dto = new com.rickbot.healing_backend.dto.DoctorRequestDto();
            dto.setEmail(req.getEmail());
            dto.setName(req.getName());
            dto.setSpecialization(req.getSpecialty());
            dto.setPhoneNumber(req.getPhone());
            dto.setAbout(req.getBio());
            // If admin provided a password, include it so update can encode and persist
            if (req.getPassword() != null && !req.getPassword().isEmpty()) {
                dto.setPassword(req.getPassword());
            }

            doctorService.updateDoctor(id, dto);
            com.rickbot.healing_backend.dto.AdminDoctorResponse r = doctorService.getAdminDoctorById(id);
            return ResponseEntity.ok(r);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Doctor not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") UUID id) {
        try {
            com.rickbot.healing_backend.dto.AdminDoctorResponse r = doctorService.getAdminDoctorById(id);
            return ResponseEntity.ok(r);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Doctor not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/verify-password")
    public ResponseEntity<?> verifyPassword(@PathVariable("id") UUID id,
                                            @RequestBody Map<String, String> body) {
        try {
            String pw = body.get("password");
            if (pw == null) return ResponseEntity.badRequest().body(Map.of("message", "password required"));
            boolean matches = doctorService.verifyDoctorPassword(id, pw);
            return ResponseEntity.ok(Map.of("matches", matches));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/migrate-plaintext-passwords")
    public ResponseEntity<?> migratePasswords(@RequestBody(required = false) Map<String, Object> body) {
        boolean confirm = body != null && Boolean.TRUE.equals(body.get("confirm"));
        if (!confirm) {
            return ResponseEntity.badRequest().body(Map.of("message", "Set {\"confirm\": true} to perform migration"));
        }
        try {
            List<Map<String, Object>> res = doctorService.migratePlaintextPasswords();
            long migratedCount = res.stream().filter(m -> Boolean.TRUE.equals(m.get("updated"))).count();
            return ResponseEntity.ok(Map.of("migrated", migratedCount, "details", res));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Server error"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Doctor not found"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batch")
    public ResponseEntity<?> batchImport(@RequestBody com.rickbot.healing_backend.dto.BulkImportRequest req) {
        try {
            List<Map<String,Object>> res = doctorService.bulkCreate(req);
            return ResponseEntity.ok(Map.of("count", res.size(), "details", res));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Server error"));
        }
    }
}
