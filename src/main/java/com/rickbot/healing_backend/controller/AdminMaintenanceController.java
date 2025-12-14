package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.service.AdminMaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMaintenanceController {

    private final AdminMaintenanceService maintenanceService;

    public AdminMaintenanceController(AdminMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, Object>> clearCache() {
        return ResponseEntity.ok(maintenanceService.clearCache());
    }

    @PostMapping("/clean-temp")
    public ResponseEntity<Map<String, Object>> cleanTemp() {
        return ResponseEntity.ok(maintenanceService.cleanTemp());
    }

    @PostMapping("/restart")
    public ResponseEntity<Map<String, Object>> restart() {
        return ResponseEntity.ok(maintenanceService.restart());
    }
}
