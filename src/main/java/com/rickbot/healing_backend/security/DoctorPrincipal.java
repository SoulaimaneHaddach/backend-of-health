package com.rickbot.healing_backend.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.rickbot.healing_backend.entity.Doctor;

import java.util.Collections;

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

    // 🔑 No roles yet → just return empty list
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
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
        return true;
    }

    public Doctor getDoctor() {
        return doctor;
    }
    
}
