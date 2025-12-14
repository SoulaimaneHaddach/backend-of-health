package com.rickbot.healing_backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.rickbot.healing_backend.entity.Doctor;

import java.util.UUID;

public class DoctorPrincipal implements UserDetails{

   private final Doctor doctor;

    public DoctorPrincipal(Doctor doctor) {
        this.doctor = doctor;
    }

    @Override
    public String getUsername() {
        return doctor.getEmail();
    }

    @Override
    public String getPassword() {
        return doctor.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assign ROLE_DOCTOR so JWT includes role and Spring Security authorizes properly
        return List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"));
    }

    // ✅ For MVP, we assume account is always valid
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Only allow login when doctor status is 'active'
        String status = doctor.getStatus();
        if (status == null) return true; // default to enabled if status not set
        return "active".equalsIgnoreCase(status);
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public UUID getId() {
        return doctor.getId();
    }
    
}
