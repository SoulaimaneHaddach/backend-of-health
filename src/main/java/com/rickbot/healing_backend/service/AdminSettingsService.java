package com.rickbot.healing_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminSettingsService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path SETTINGS_PATH = Paths.get("data", "admin-settings.json");
    private final Map<String, Object> settings = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            Path parent = SETTINGS_PATH.getParent();
            if (parent != null) Files.createDirectories(parent);

            if (Files.exists(SETTINGS_PATH)) {
                Map<String, Object> loaded = MAPPER.readValue(SETTINGS_PATH.toFile(), new TypeReference<Map<String, Object>>() {});
                if (loaded != null) settings.putAll(loaded);
            } else {
                // defaults
                settings.put("maintenanceMode", false);
                settings.put("emailFrom", "no-reply@example.com");
                settings.put("theme", "default");
                persist();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize admin settings", e);
        }
    }

    public Map<String, Object> getSettings() {
        return Collections.unmodifiableMap(new HashMap<>(settings));
    }

    public synchronized Map<String, Object> updateSettings(Map<String, Object> payload) {
        if (payload == null) return getSettings();
        settings.putAll(payload);
        persist();
        return getSettings();
    }

    public synchronized Map<String, Object> setMaintenance(boolean enabled) {
        settings.put("maintenanceMode", enabled);
        persist();
        return getSettings();
    }

    private void persist() {
        try {
            Path parent = SETTINGS_PATH.getParent();
            if (parent != null) Files.createDirectories(parent);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(SETTINGS_PATH.toFile(), settings);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist admin settings", e);
        }
    }
}
