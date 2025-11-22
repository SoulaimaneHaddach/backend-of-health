package com.rickbot.healing_backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Value("${app.cache.default-spec:maximumSize=10000,expireAfterWrite=10m}")
    private String defaultSpec;

    @Value("${app.cache.doctors-spec:maximumSize=2000,expireAfterWrite=10m}")
    private String doctorsSpec;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        // create builders
        Caffeine<Object, Object> doctorsBuilder = caffeineFromSpec(doctorsSpec);

        List<org.springframework.cache.Cache> caches = new ArrayList<>();
        caches.add(new CaffeineCache("doctorsCache", doctorsBuilder.build()));
        // you can add more named caches here and map them to per-cache specs

        manager.setCaches(caches);
        return manager;
    }

    private Caffeine<Object, Object> caffeineFromSpec(String spec) {
        // parse simple spec like "maximumSize=10000,expireAfterWrite=10m"
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        String[] parts = spec.split(",");
        for (String p : parts) {
            String t = p.trim();
            if (t.startsWith("maximumSize=")) {
                long size = Long.parseLong(t.split("=")[1]);
                builder.maximumSize(size);
            } else if (t.startsWith("expireAfterWrite=")) {
                String v = t.split("=")[1];
                if (v.endsWith("m")) {
                    long min = Long.parseLong(v.substring(0, v.length() - 1));
                    builder.expireAfterWrite(min, TimeUnit.MINUTES);
                } else if (v.endsWith("s")) {
                    long sec = Long.parseLong(v.substring(0, v.length() - 1));
                    builder.expireAfterWrite(sec, TimeUnit.SECONDS);
                }
            }
        }

        return builder.recordStats();
    }
}
