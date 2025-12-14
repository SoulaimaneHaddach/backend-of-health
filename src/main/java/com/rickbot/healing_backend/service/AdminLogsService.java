package com.rickbot.healing_backend.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdminLogsService {

    private final Path logsDir = Paths.get("logs");
    private final Pattern levelPattern = Pattern.compile("\\b(INFO|WARN|ERROR|DEBUG|TRACE)\\b", Pattern.CASE_INSENSITIVE);

    public static class LogEntry {
        public String id;
        public String time;
        public String level;
        public String message;
        public String file;
    }

    public Map<String, Object> list(int page, int size, String levelFilter, String q) {
        try {
            if (!Files.exists(logsDir) || !Files.isDirectory(logsDir)) {
                return Map.of("items", List.of(), "total", 0, "page", page, "size", size);
            }

            List<LogEntry> all = new ArrayList<>();

            var files = Files.list(logsDir).filter(Files::isRegularFile).collect(Collectors.toList());

            // sort files by last modified desc so newest logs appear first
            files.sort(Comparator.comparingLong(p -> -p.toFile().lastModified()));

            for (Path f : files) {
                List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.isEmpty()) continue;
                    LogEntry e = new LogEntry();
                    e.file = f.getFileName().toString();
                    e.id = e.file + ":" + i;
                    if (line.length() >= 19) e.time = line.substring(0, 19);
                    else e.time = "";
                    Matcher m = levelPattern.matcher(line);
                    if (m.find()) e.level = m.group(1).toUpperCase(); else e.level = "INFO";
                    // message: everything after the level if present
                    int idx = line.indexOf(e.level);
                    if (idx >= 0) {
                        int after = idx + e.level.length();
                        e.message = line.substring(Math.min(after + 1, line.length())).trim();
                    } else {
                        e.message = line;
                    }
                    all.add(e);
                }
            }

            // apply filters
            List<LogEntry> filtered = all.stream().filter(le -> {
                if (levelFilter != null && !levelFilter.isBlank()) {
                    if (!le.level.equalsIgnoreCase(levelFilter)) return false;
                }
                if (q != null && !q.isBlank()) {
                    String qq = q.toLowerCase();
                    if (!(le.message.toLowerCase().contains(qq) || le.file.toLowerCase().contains(qq) || le.time.toLowerCase().contains(qq))) return false;
                }
                return true;
            }).collect(Collectors.toList());

            int total = filtered.size();
            int from = Math.max(0, (page - 1) * size);
            int to = Math.min(total, from + size);
            List<LogEntry> pageItems = from < to ? filtered.subList(from, to) : List.of();

            return Map.of("items", pageItems, "total", total, "page", page, "size", size);

        } catch (IOException ex) {
            throw new RuntimeException("Failed reading logs", ex);
        }
    }
}
