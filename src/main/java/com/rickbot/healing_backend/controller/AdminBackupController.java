package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.service.AdminBackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/admin/backup")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBackupController {

    private final AdminBackupService backupService;

    public AdminBackupController(AdminBackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBackup() {
        Map<String, Object> res = backupService.createBackup();
        return ResponseEntity.ok(res);
    }

    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(@RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                Map<String, Object> res = backupService.restoreFromUpload(zis);
                return ResponseEntity.ok(res);
            }
        } else {
            Map<String, Object> res = backupService.restoreLatest();
            return ResponseEntity.ok(res);
        }
    }
}
