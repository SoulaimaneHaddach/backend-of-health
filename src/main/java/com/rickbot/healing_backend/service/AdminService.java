package com.rickbot.healing_backend.service;

import com.rickbot.healing_backend.dto.AdminUserRequest;
import com.rickbot.healing_backend.dto.AdminUserResponse;
import com.rickbot.healing_backend.entity.Admin;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.repository.AdminRepository;
import com.rickbot.healing_backend.repository.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepo doctorRepo;

    // --- Admin user management ---
    public AdminUserResponse create(AdminUserRequest req) {
        Admin a = new Admin();
        a.setEmail(req.getEmail());
        a.setName(req.getName());
        a.setPassword(passwordEncoder.encode(req.getPassword()));
        a.setRoles(req.getRoles());
        a.setEnabled(true);
        Admin saved = adminRepository.save(a);
        return toDto(saved);
    }

    public List<AdminUserResponse> list() {
        return adminRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public java.util.Map<String, Object> list(String q, int page, int size, String sortBy, String sortDir) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            org.springframework.data.domain.Sort.Direction d = "desc".equalsIgnoreCase(sortDir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
            sort = org.springframework.data.domain.Sort.by(d, sortBy);
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size), sort);
        org.springframework.data.domain.Page<Admin> p;
        if (q != null && !q.isBlank()) {
            p = adminRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
        } else {
            p = adminRepository.findAll(pageable);
        }
        java.util.List<AdminUserResponse> items = p.stream().map(this::toDto).collect(Collectors.toList());
        java.util.Map<String,Object> out = new java.util.HashMap<>();
        out.put("items", items);
        out.put("total", p.getTotalElements());
        out.put("page", p.getNumber());
        out.put("size", p.getSize());
        return out;
    }

    public long bulkDelete(java.util.List<java.util.UUID> ids) {
        if (ids == null || ids.isEmpty()) return 0L;
        try {
            adminRepository.deleteAllByIdInBatch(ids);
        } catch (NoSuchMethodError | java.lang.NoClassDefFoundError ex) {
            adminRepository.deleteAllById(ids);
        }
        return ids.size();
    }

    public Optional<AdminUserResponse> findById(UUID id) {
        return adminRepository.findById(id).map(this::toDto);
    }

    public Optional<Admin> findEntityByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    public Optional<AdminUserResponse> update(UUID id, AdminUserRequest req) {
        return adminRepository.findById(id).map(a -> {
            if (req.getEmail() != null) a.setEmail(req.getEmail());
            if (req.getName() != null) a.setName(req.getName());
            if (req.getPassword() != null) a.setPassword(passwordEncoder.encode(req.getPassword()));
            if (req.getRoles() != null) a.setRoles(req.getRoles());
            Admin saved = adminRepository.save(a);
            return toDto(saved);
        });
    }

    public Optional<AdminUserResponse> ban(UUID id) {
        return adminRepository.findById(id).map(a -> {
            a.setEnabled(false);
            return toDto(adminRepository.save(a));
        });
    }

    public Optional<AdminUserResponse> unban(UUID id) {
        return adminRepository.findById(id).map(a -> {
            a.setEnabled(true);
            return toDto(adminRepository.save(a));
        });
    }

    public boolean delete(UUID id) {
        if (adminRepository.existsById(id)) {
            adminRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public AdminUserResponse toDto(Admin a) {
        AdminUserResponse r = new AdminUserResponse();
        r.setId(a.getId());
        r.setEmail(a.getEmail());
        r.setName(a.getName());
        r.setEnabled(a.isEnabled());
        r.setRoles(a.getRoles());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }

    // --- Doctor verification helpers (origin/main additions) ---
    public void verifyDoctor(UUID doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        doctor.setVerified(true);
        doctorRepo.save(doctor);
    }

    public void unverifyDoctor(UUID doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        doctor.setVerified(false);
        doctorRepo.save(doctor);
    }
}
package com.rickbot.healing_backend.service;

<<<<<<< HEAD
import com.rickbot.healing_backend.dto.AdminUserRequest;
import com.rickbot.healing_backend.dto.AdminUserResponse;
import com.rickbot.healing_backend.entity.Admin;
import com.rickbot.healing_backend.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
=======
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.repository.DoctorRepo;

import lombok.RequiredArgsConstructor;
>>>>>>> origin/main

@Service
@RequiredArgsConstructor
public class AdminService {
<<<<<<< HEAD

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserResponse create(AdminUserRequest req) {
        Admin a = new Admin();
        a.setEmail(req.getEmail());
        a.setName(req.getName());
        a.setPassword(passwordEncoder.encode(req.getPassword()));
        a.setRoles(req.getRoles());
        a.setEnabled(true);
        Admin saved = adminRepository.save(a);
        return toDto(saved);
    }

    public List<AdminUserResponse> list() {
        return adminRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public java.util.Map<String, Object> list(String q, int page, int size, String sortBy, String sortDir) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            org.springframework.data.domain.Sort.Direction d = "desc".equalsIgnoreCase(sortDir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
            sort = org.springframework.data.domain.Sort.by(d, sortBy);
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size), sort);
        org.springframework.data.domain.Page<Admin> p;
        if (q != null && !q.isBlank()) {
            p = adminRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
        } else {
            p = adminRepository.findAll(pageable);
        }
        java.util.List<AdminUserResponse> items = p.stream().map(this::toDto).collect(Collectors.toList());
        java.util.Map<String,Object> out = new java.util.HashMap<>();
        out.put("items", items);
        out.put("total", p.getTotalElements());
        out.put("page", p.getNumber());
        out.put("size", p.getSize());
        return out;
    }

    public long bulkDelete(java.util.List<java.util.UUID> ids) {
        if (ids == null || ids.isEmpty()) return 0L;
        // use deleteAllByIdInBatch if available for performance
        try {
            adminRepository.deleteAllByIdInBatch(ids);
        } catch (NoSuchMethodError | java.lang.NoClassDefFoundError ex) {
            // fallback
            adminRepository.deleteAllById(ids);
        }
        return ids.size();
    }

    public Optional<AdminUserResponse> findById(UUID id) {
        return adminRepository.findById(id).map(this::toDto);
    }

    public Optional<Admin> findEntityByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    public Optional<AdminUserResponse> update(UUID id, AdminUserRequest req) {
        return adminRepository.findById(id).map(a -> {
            if (req.getEmail() != null) a.setEmail(req.getEmail());
            if (req.getName() != null) a.setName(req.getName());
            if (req.getPassword() != null) a.setPassword(passwordEncoder.encode(req.getPassword()));
            if (req.getRoles() != null) a.setRoles(req.getRoles());
            Admin saved = adminRepository.save(a);
            return toDto(saved);
        });
    }

    public Optional<AdminUserResponse> ban(UUID id) {
        return adminRepository.findById(id).map(a -> {
            a.setEnabled(false);
            return toDto(adminRepository.save(a));
        });
    }

    public Optional<AdminUserResponse> unban(UUID id) {
        return adminRepository.findById(id).map(a -> {
            a.setEnabled(true);
            return toDto(adminRepository.save(a));
        });
    }

    public boolean delete(UUID id) {
        if (adminRepository.existsById(id)) {
            adminRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public AdminUserResponse toDto(Admin a) {
        AdminUserResponse r = new AdminUserResponse();
        r.setId(a.getId());
        r.setEmail(a.getEmail());
        r.setName(a.getName());
        r.setEnabled(a.isEnabled());
        r.setRoles(a.getRoles());
        r.setCreatedAt(a.getCreatedAt());
        return r;
=======
    
    private final DoctorRepo doctorRepo;
    public void verifyDoctor(UUID doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        doctor.setVerified(true);
        doctorRepo.save(doctor);
    }

    public void unverifyDoctor(UUID doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        doctor.setVerified(false);
        doctorRepo.save(doctor);
>>>>>>> origin/main
    }
}
