package com.rickbot.healing_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.rickbot.healing_backend.dto.BookingRequestDTO;
import com.rickbot.healing_backend.entity.Book;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {
    
    Book toEntity(BookingRequestDTO dto);
}
