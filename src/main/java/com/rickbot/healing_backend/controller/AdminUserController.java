package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.dto.AdminUserRequest;
import com.rickbot.healing_backend.dto.AdminUserResponse;
import com.rickbot.healing_backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list(@RequestParam(name = "q", required = false) String q,
                                  @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                  @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                  @RequestParam(name = "sortBy", required = false) String sortBy,
                                  @RequestParam(name = "sortDir", required = false, defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(adminService.list(q, page, size, sortBy, sortDir));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkDelete(@RequestBody java.util.Map<String, java.util.List<java.util.UUID>> body) {
        java.util.List<java.util.UUID> ids = body == null ? java.util.Collections.emptyList() : body.getOrDefault("ids", java.util.Collections.emptyList());
        long deleted = adminService.bulkDelete(ids);
        return ResponseEntity.ok(java.util.Map.of("deleted", deleted));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        var opt = adminService.findById(id);
        if (opt.isPresent()) return ResponseEntity.ok(opt.get());
        return ResponseEntity.status(404).body(Map.of("message", "Admin not found"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody AdminUserRequest req) {
        var created = adminService.create(req);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody AdminUserRequest req) {
        var opt = adminService.update(id, req);
        if (opt.isPresent()) return ResponseEntity.ok(opt.get());
        return ResponseEntity.status(404).body(Map.of("message", "Admin not found"));
    }

    @PostMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> ban(@PathVariable UUID id) {
        var opt = adminService.ban(id);
        if (opt.isPresent()) return ResponseEntity.ok(opt.get());
        return ResponseEntity.status(404).body(Map.of("message", "Admin not found"));
    }

    @PostMapping("/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unban(@PathVariable UUID id) {
        var opt = adminService.unban(id);
        if (opt.isPresent()) return ResponseEntity.ok(opt.get());
        return ResponseEntity.status(404).body(Map.of("message", "Admin not found"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        boolean ok = adminService.delete(id);
        if (ok) return ResponseEntity.noContent().build();
        return ResponseEntity.status(404).body(Map.of("message", "Admin not found"));
    }

    // Fallback for environments that disallow DELETE requests (proxies, older clients)
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteViaPost(@PathVariable UUID id) {
        boolean ok = adminService.delete(id);
        if (ok) return ResponseEntity.noContent().build();
        return ResponseEntity.status(404).body(Map.of("message", "Admin not found"));
    }
}
