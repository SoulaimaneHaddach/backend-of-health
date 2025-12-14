package com.rickbot.healing_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public class AdminUserRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String name;

    private Set<String> roles;

    public AdminUserRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
