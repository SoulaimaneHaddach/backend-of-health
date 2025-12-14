package com.rickbot.healing_backend.dto;

import java.util.List;
import java.util.Map;

public class AdminMessageRequest {
    public List<String> recipients;
    public String template;
    public String body;
    public String subject;
    public String channel; // e.g., email, sms
    public Map<String, Object> meta;
}
