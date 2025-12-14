package com.rickbot.healing_backend.service;

import com.rickbot.healing_backend.dto.AdminSpecialtyRequest;
import com.rickbot.healing_backend.dto.AdminSpecialtyResponse;
import com.rickbot.healing_backend.entity.Specialty;
import com.rickbot.healing_backend.repository.SpecialtyRepository;
import com.rickbot.healing_backend.exception.NoResourceFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpecialtyService {
    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    public List<AdminSpecialtyResponse> getAll() {
        return specialtyRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AdminSpecialtyResponse findById(UUID id) {
        Specialty s = specialtyRepository.findById(id).orElseThrow(() -> new NoResourceFoundException("Specialty not found"));
        return toResponse(s);
    }

    public AdminSpecialtyResponse create(AdminSpecialtyRequest req) {
        Specialty s = new Specialty();
        s.setName(req.getName());
        s.setDescription(req.getDescription());
        Specialty saved = specialtyRepository.save(s);
        return toResponse(saved);
    }

    public AdminSpecialtyResponse update(UUID id, AdminSpecialtyRequest req) {
        Specialty s = specialtyRepository.findById(id).orElseThrow(() -> new NoResourceFoundException("Specialty not found"));
        if (req.getName() != null) s.setName(req.getName());
        if (req.getDescription() != null) s.setDescription(req.getDescription());
        Specialty saved = specialtyRepository.save(s);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        if (!specialtyRepository.existsById(id)) throw new NoResourceFoundException("Specialty not found");
        specialtyRepository.deleteById(id);
    }

    private AdminSpecialtyResponse toResponse(Specialty s) {
        AdminSpecialtyResponse r = new AdminSpecialtyResponse();
        r.setId(s.getId() == null ? null : s.getId().toString());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
