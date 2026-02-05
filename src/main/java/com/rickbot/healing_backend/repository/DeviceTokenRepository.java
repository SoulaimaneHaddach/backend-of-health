package com.rickbot.healing_backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rickbot.healing_backend.entity.DeviceToken;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    Optional<DeviceToken> findByUserIdAndPlatform(UUID userId, String platform);
    Optional<DeviceToken> findByToken(String token);
    List<DeviceToken> findAllByUserId(UUID userId);
}
