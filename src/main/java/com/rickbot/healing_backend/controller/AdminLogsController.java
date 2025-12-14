package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.service.AdminLogsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogsController {

    private final AdminLogsService logsService;

    public AdminLogsController(AdminLogsService logsService) {
        this.logsService = logsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String q
    ) {
        Map<String, Object> res = logsService.list(page, size, level, q);
        return ResponseEntity.ok(res);
    }
}
