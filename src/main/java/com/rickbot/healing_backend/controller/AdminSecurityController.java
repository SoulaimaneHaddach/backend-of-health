package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.dto.AdminSecurityRequest;
import com.rickbot.healing_backend.service.AdminSecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/security")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSecurityController {

    private final AdminSecurityService securityService;

    public AdminSecurityController(AdminSecurityService securityService) {
        this.securityService = securityService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> get() {
        return ResponseEntity.ok(securityService.getSecuritySettings());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody AdminSecurityRequest req) {
        return ResponseEntity.ok(securityService.updateSecuritySettings(req));
    }
}
