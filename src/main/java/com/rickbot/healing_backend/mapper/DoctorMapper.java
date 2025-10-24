package com.rickbot.healing_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.entity.Doctor;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DoctorMapper {

    DoctorResponseDto toDto(Doctor entity);

    Doctor toEntity(DoctorRequestDto dto);

    void updateEntityFromDto(DoctorRequestDto dto, @MappingTarget Doctor doctor);

    void updateDoctorFromDto(DoctorRequestDto dto, @MappingTarget Doctor doctor);
}
