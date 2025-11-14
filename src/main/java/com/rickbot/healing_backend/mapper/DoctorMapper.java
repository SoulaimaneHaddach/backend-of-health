package com.rickbot.healing_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.AfterMapping;

import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.dto.DoctorMapDto;
import com.rickbot.healing_backend.entity.Doctor;

import java.util.ArrayList;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DoctorMapper {
    
    public abstract DoctorResponseDto toDto(Doctor entity);

    // Ensure null-safe mapping with defaults for Android compatibility
    @AfterMapping
    protected void ensureNonNullValues(Doctor source, @MappingTarget DoctorResponseDto target) {
        if (target.getPhoneNumber() == null) target.setPhoneNumber("");
        if (target.getAbout() == null) target.setAbout("");
        if (target.getHours() == null) target.setHours(new ArrayList<>());
        if (target.getServices() == null) target.setServices(new ArrayList<>());
        if (target.getRating() == null) target.setRating(0.0);
        if (target.getReviewsCount() == null) target.setReviewsCount(0);
        if (target.getName() == null) target.setName("");
        if (target.getEmail() == null) target.setEmail("");
        if (target.getSpecialization() == null) target.setSpecialization("");
    }

    // Lightweight mapping for map markers
    public abstract DoctorMapDto toMapDto(Doctor entity);
    
    @AfterMapping
    protected void ensureMapDtoNonNull(Doctor source, @MappingTarget DoctorMapDto target) {
        if (target.getName() == null) target.setName("");
        if (target.getEmail() == null) target.setEmail("");
        if (target.getSpecialization() == null) target.setSpecialization("");
    }
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Doctor toEntity(DoctorRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntityFromDto(DoctorRequestDto dto, @MappingTarget Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateDoctorFromDto(DoctorRequestDto dto, @MappingTarget Doctor doctor);
}
