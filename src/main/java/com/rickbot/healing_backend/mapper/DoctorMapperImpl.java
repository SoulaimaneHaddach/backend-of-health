package com.rickbot.healing_backend.mapper;

import org.springframework.stereotype.Component;

import com.rickbot.healing_backend.dto.AdminDoctorResponse;
import com.rickbot.healing_backend.dto.DoctorMapDto;
import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.entity.Doctor;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class DoctorMapperImpl extends DoctorMapper {

    @Override
    public DoctorResponseDto toDto(Doctor entity) {
        if (entity == null) return null;
        DoctorResponseDto dto = new DoctorResponseDto();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail() == null ? "" : entity.getEmail());
        dto.setName(entity.getName() == null ? "" : entity.getName());
        dto.setSpecialization(entity.getSpecialization() == null ? "" : entity.getSpecialization());
        dto.setPhoneNumber(entity.getPhoneNumber() == null ? "" : entity.getPhoneNumber());
        dto.setLocation(entity.getLocation());
        dto.setRating(entity.getRating() == null ? 0.0 : entity.getRating());
        dto.setReviewsCount(entity.getReviewsCount() == null ? 0 : entity.getReviewsCount());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setAbout(entity.getAbout() == null ? "" : entity.getAbout());
        dto.setHours(entity.getHours() == null ? new ArrayList<>() : entity.getHours());
        dto.setServices(entity.getServices() == null ? new ArrayList<>() : entity.getServices());
        return dto;
    }

    @Override
    public DoctorMapDto toMapDto(Doctor entity) {
        if (entity == null) return null;
        DoctorMapDto dto = new DoctorMapDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName() == null ? "" : entity.getName());
        dto.setEmail(entity.getEmail() == null ? "" : entity.getEmail());
        dto.setSpecialization(entity.getSpecialization() == null ? "" : entity.getSpecialization());
        dto.setLocation(entity.getLocation());
        return dto;
    }

    @Override
    public Doctor toEntity(DoctorRequestDto dto) {
        if (dto == null) return null;
        Doctor d = new Doctor();
        d.setEmail(dto.getEmail());
        d.setName(dto.getName());
        d.setSpecialization(dto.getSpecialization());
        d.setPhoneNumber(dto.getPhoneNumber());
        d.setLocation(dto.getLocation());
        d.setRating(dto.getRating());
        d.setReviewsCount(dto.getReviewsCount());
        d.setAbout(dto.getAbout());
        d.setHours(dto.getHours());
        d.setServices(dto.getServices());
        return d;
    }

    @Override
    public void updateEntityFromDto(DoctorRequestDto dto, Doctor doctor) {
        if (dto == null || doctor == null) return;
        if (dto.getEmail() != null) doctor.setEmail(dto.getEmail());
        if (dto.getName() != null) doctor.setName(dto.getName());
        if (dto.getSpecialization() != null) doctor.setSpecialization(dto.getSpecialization());
        if (dto.getPhoneNumber() != null) doctor.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getLocation() != null) doctor.setLocation(dto.getLocation());
        if (dto.getRating() != null) doctor.setRating(dto.getRating());
        if (dto.getReviewsCount() != null) doctor.setReviewsCount(dto.getReviewsCount());
        if (dto.getAbout() != null) doctor.setAbout(dto.getAbout());
        if (dto.getHours() != null) doctor.setHours(dto.getHours());
        if (dto.getServices() != null) doctor.setServices(dto.getServices());
    }

    @Override
    public void updateDoctorFromDto(DoctorRequestDto dto, Doctor doctor) {
        // Same behaviour as updateEntityFromDto for now
        updateEntityFromDto(dto, doctor);
    }

    @Override
    public AdminDoctorResponse toAdminDoctorResponse(Doctor entity) {
        if (entity == null) return null;
        AdminDoctorResponse r = new AdminDoctorResponse();
        r.setId(entity.getId() != null ? entity.getId().toString() : null);
        r.setName(entity.getName());
        r.setEmail(entity.getEmail());
        r.setSpecialty(entity.getSpecialization());
        r.setStatus(entity.getStatus() != null ? entity.getStatus() : "pending");
        r.setBookings(entity.getBookings() != null ? entity.getBookings() : 0);
        r.setCreatedAt(entity.getCreatedAt());
        return r;
    }
}
