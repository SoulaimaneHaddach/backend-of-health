package com.rickbot.healing_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminMaintenanceService {

    private final AdminSettingsService settingsService;
    private final CacheManager cacheManager;

    public Map<String, Object> clearCache() {
        List<String> cleared = new ArrayList<>();
        if (cacheManager != null) {
            for (String name : cacheManager.getCacheNames()) {
                Cache c = cacheManager.getCache(name);
                if (c != null) {
                    try {
                        c.clear();
                        cleared.add(name);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        Map<String, Object> out = new HashMap<>();
        out.put("cleared", cleared);
        out.put("count", cleared.size());
        return out;
    }

    public Map<String, Object> cleanTemp() {
        List<String> deleted = new ArrayList<>();
        // candidates: data/tmp, tmp
        List<Path> candidates = new ArrayList<>();
        candidates.add(Paths.get("data", "tmp"));
        candidates.add(Paths.get("tmp"));

        for (Path p : candidates) {
            if (Files.exists(p)) {
                try {
                    int[] count = new int[]{0};
                    Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            try {
                                Files.deleteIfExists(file);
                                deleted.add(file.toString());
                                count[0]++;
                            } catch (IOException ignored) {
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            try {
                                if (!dir.equals(p)) Files.deleteIfExists(dir);
                            } catch (IOException ignored) {
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException ignored) {
                }
            }
        }

        Map<String, Object> out = new HashMap<>();
        out.put("deleted", deleted);
        out.put("count", deleted.size());
        return out;
    }

    public Map<String, Object> restart() {
        // persist maintenance flag so external process managers or monitors can notice
        settingsService.updateSettings(Map.of("maintenanceMode", true));

        // schedule shutdown in background to allow the HTTP response to return
        new Thread(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
            }
            // Exiting the JVM; rely on process manager to restart the application.
            System.exit(0);
        }, "admin-restart-thread").start();

        return Map.of("restarting", true, "message", "Application will exit shortly (1s). Ensure a process manager restarts it.");
    }
}

