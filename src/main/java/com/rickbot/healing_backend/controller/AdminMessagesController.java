package com.rickbot.healing_backend.controller;

import com.rickbot.healing_backend.dto.AdminMessageRequest;
import com.rickbot.healing_backend.service.AdminMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/messages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessagesController {

    private final AdminMessageService messageService;

    public AdminMessagesController(AdminMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody AdminMessageRequest payload) {
        return ResponseEntity.ok(messageService.sendMessage(payload));
    }

    @GetMapping
    public ResponseEntity<java.util.Map<String, Object>> listMessages(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "recipient", required = false) String recipient,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(messageService.listMessages(page, size, q, recipient));
    }
}
