package com.rickbot.healing_backend.config;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rickbot.healing_backend.mapper.BookMapper;

@Configuration
public class MapperConfig {

    @Bean
    public BookMapper bookMapper() {
        return Mappers.getMapper(BookMapper.class);
    }
}
