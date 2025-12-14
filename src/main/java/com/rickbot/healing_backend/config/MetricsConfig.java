package com.rickbot.healing_backend.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    @Value("${spring.application.name:healing_backend}")
    private String applicationName;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        meterRegistry.config().commonTags("application", applicationName);
    }
}
