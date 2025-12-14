package com.rickbot.healing_backend.service;

import com.rickbot.healing_backend.dto.AdminSecurityRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminSecurityService {

    private final AdminSettingsService settingsService;

    public Map<String, Object> getSecuritySettings() {
        Map<String, Object> all = settingsService.getSettings();
        Object sec = all.get("security");
        if (sec instanceof Map) {
            //noinspection unchecked
            return new HashMap<>((Map<String, Object>) sec);
        }
        return new HashMap<>();
    }

    public Map<String, Object> updateSecuritySettings(AdminSecurityRequest req) {
        Map<String, Object> sec = getSecuritySettings();
        if (req == null) return settingsService.updateSettings(Map.of("security", sec));
        if (req.twoFactorEnabled != null) sec.put("twoFactorEnabled", req.twoFactorEnabled);
        if (req.allowPasswordLogin != null) sec.put("allowPasswordLogin", req.allowPasswordLogin);
        if (req.extras != null) sec.putAll(req.extras);
        return settingsService.updateSettings(Map.of("security", sec));
    }
}
