package com.rickbot.healing_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.rickbot.healing_backend.dto.BookingRequestDTO;
import com.rickbot.healing_backend.entity.Book;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    Book toEntity(BookingRequestDTO dto);
}
