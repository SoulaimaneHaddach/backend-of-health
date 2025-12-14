package com.rickbot.healing_backend.service;

import java.util.List;

public class AdminUser {
    private Long id;
    private String username;
    private String displayName;
    private List<String> roles;
    private List<String> perms;

    public AdminUser() {}

    public AdminUser(Long id, String username, String displayName, List<String> roles, List<String> perms) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.roles = roles;
        this.perms = perms;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<String> getPerms() { return perms; }
    public void setPerms(List<String> perms) { this.perms = perms; }
}
