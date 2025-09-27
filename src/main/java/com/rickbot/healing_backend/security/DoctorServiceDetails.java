package com.rickbot.healing_backend.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.repository.DoctorRepo;

import lombok.Data;

@Service
@Data
public class DoctorServiceDetails implements UserDetailsService {
    private final DoctorRepo doctorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return doctorRepository.findByEmail(email)
                .map(DoctorPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with email: " + email));
    }

    public DoctorPrincipal loadById(UUID id) {
        Doctor doc = doctorRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found id: " + id));
        return new DoctorPrincipal(doc);
    }
}
