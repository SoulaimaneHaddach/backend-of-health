package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.service.AdminSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final AdminSettingsService settingsService;

    public AdminSettingsController(AdminSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(settingsService.updateSettings(payload));
    }

    @PostMapping("/maintenance")
    public ResponseEntity<Map<String, Object>> setMaintenance(@RequestBody Map<String, Object> payload) {
        Boolean enabled = false;
        if (payload != null && payload.get("maintenanceMode") instanceof Boolean) {
            enabled = (Boolean) payload.get("maintenanceMode");
        }
        return ResponseEntity.ok(settingsService.setMaintenance(enabled));
    }
}
