package com.rickbot.healing_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.notifications")
@Data
public class NotificationsConfig {

    private Push push = new Push();

    @Data
    public static class Push {
        private boolean enabled = true;
    }
}
