package com.rickbot.healing_backend.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.pattern.ClassicConverter;

/**
 * Custom logback converter that renders an emoji for the logging level.
 * Used in logback-spring.xml as %levelEmoji
 */
public class LevelEmojiConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        String level = event.getLevel().toString();
        return switch (level) {
            case "TRACE" -> "🔍";
            case "DEBUG" -> "🐞";
            case "INFO"  -> "ℹ️";
            case "WARN"  -> "⚠️";
            case "ERROR" -> "❌";
            default -> "";
        };
    }
}
