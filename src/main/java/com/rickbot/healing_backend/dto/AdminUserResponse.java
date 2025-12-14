package com.rickbot.healing_backend.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class AdminUserResponse {
    private UUID id;
    private String email;
    private String name;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;

    public AdminUserResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
