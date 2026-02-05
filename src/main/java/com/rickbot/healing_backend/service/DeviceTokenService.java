package com.rickbot.healing_backend.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rickbot.healing_backend.dto.RegisterDeviceRequest;
import com.rickbot.healing_backend.entity.DeviceToken;
import com.rickbot.healing_backend.repository.DeviceTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private static final Logger log = LoggerFactory.getLogger(DeviceTokenService.class);

    private final DeviceTokenRepository repository;

    @Transactional
    public DeviceToken registerDeviceForUser(java.util.UUID userUuid, RegisterDeviceRequest request) {
        if (request.getToken() == null || request.getPlatform() == null) {
            throw new IllegalArgumentException("token and platform are required");
        }

        // If there's already a token for this user+platform, update it
        Optional<DeviceToken> existing = repository.findByUserIdAndPlatform(userUuid, request.getPlatform());
        DeviceToken device;
        if (existing.isPresent()) {
            device = existing.get();
            device.setToken(request.getToken());
            log.info("Updating device token for user {} platform={}", userUuid, request.getPlatform());
        } else {
            device = new DeviceToken();
            device.setId(UUID.randomUUID());
            device.setUserId(userUuid);
            device.setPlatform(request.getPlatform());
            device.setToken(request.getToken());
            log.info("Registering new device token for user {} platform={}", userUuid, request.getPlatform());
        }

        DeviceToken saved = repository.save(device);
        log.debug("Saved device token id={} user={}", saved.getId(), saved.getUserId());
        return saved;
    }
}
