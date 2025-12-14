package com.rickbot.healing_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rickbot.healing_backend.dto.AdminMessageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminMessageService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File messagesFile = new File("data/messages.log");

    public Map<String, Object> sendMessage(AdminMessageRequest req) {
        try {
            // Ensure directory exists
            File dir = messagesFile.getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();

            String id = UUID.randomUUID().toString();
            Map<String, Object> record = new HashMap<>();
            record.put("id", id);
            record.put("timestamp", Instant.now().toString());
            record.put("recipients", req.recipients);
            record.put("template", req.template);
            record.put("subject", req.subject);
            record.put("body", req.body);
            record.put("channel", req.channel);
            record.put("meta", req.meta);

            // Append JSON line
            try (FileWriter fw = new FileWriter(messagesFile, true)) {
                fw.write(mapper.writeValueAsString(record));
                fw.write(System.lineSeparator());
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("sent", true);
            resp.put("id", id);
            return resp;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to record message", ex);
        }
    }

    public java.util.Map<String, Object> listMessages(int page, int size, String q, String recipient) {
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        if (!messagesFile.exists()) {
            java.util.Map<String, Object> out = new java.util.HashMap<>();
            out.put("items", items);
            out.put("total", 0);
            out.put("page", Math.max(0, page));
            out.put("size", Math.max(1, size));
            return out;
        }

        try (java.util.stream.Stream<String> lines = java.nio.file.Files.lines(messagesFile.toPath())) {
            lines.forEach(line -> {
                try {
                    if (line == null || line.isBlank()) return;
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(line);
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> record = mapper.convertValue(node, java.util.Map.class);

                    boolean matches = true;
                    if (q != null && !q.isBlank()) {
                        String ql = q.toLowerCase();
                        String subject = record.getOrDefault("subject", "").toString().toLowerCase();
                        String body = record.getOrDefault("body", "").toString().toLowerCase();
                        String template = record.getOrDefault("template", "").toString().toLowerCase();
                        if (!(subject.contains(ql) || body.contains(ql) || template.contains(ql))) matches = false;
                    }
                    if (matches && recipient != null && !recipient.isBlank()) {
                        Object recs = record.get("recipients");
                        boolean found = false;
                        if (recs instanceof java.util.Collection) {
                            for (Object r : (java.util.Collection) recs) {
                                if (r != null && r.toString().toLowerCase().contains(recipient.toLowerCase())) { found = true; break; }
                            }
                        } else if (recs != null) {
                            if (recs.toString().toLowerCase().contains(recipient.toLowerCase())) found = true;
                        }
                        if (!found) matches = false;
                    }

                    if (matches) items.add(record);
                } catch (Exception ignored) {
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read messages log", e);
        }

        int total = items.size();
        int ps = Math.max(1, size);
        int pg = Math.max(0, page);
        int from = pg * ps;
        int to = Math.min(total, from + ps);
        java.util.List<java.util.Map<String, Object>> pageItems = new java.util.ArrayList<>();
        if (from < total) pageItems = items.subList(from, to);

        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("items", pageItems);
        out.put("total", total);
        out.put("page", pg);
        out.put("size", ps);
        return out;
    }
}
