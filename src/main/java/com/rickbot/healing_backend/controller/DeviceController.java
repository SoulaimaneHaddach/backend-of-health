package com.rickbot.healing_backend.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.dto.RegisterDeviceRequest;
import com.rickbot.healing_backend.entity.DeviceToken;
import com.rickbot.healing_backend.security.DoctorPrincipal;
import com.rickbot.healing_backend.service.DeviceTokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    private final DeviceTokenService deviceTokenService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerDevice(@RequestBody RegisterDeviceRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof DoctorPrincipal)) {
            return ResponseEntity.status(401).build();
        }

        DoctorPrincipal principal = (DoctorPrincipal) auth.getPrincipal();
        UUID userId = principal.getId();

        log.info("Device register attempt by user {} platform={}", userId, request.getPlatform());
        DeviceToken saved = deviceTokenService.registerDeviceForUser(userId, request);
        log.info("Device registered id={} user={}", saved.getId(), userId);
        return ResponseEntity.created(URI.create("/devices/" + saved.getId())).build();
    }
}
