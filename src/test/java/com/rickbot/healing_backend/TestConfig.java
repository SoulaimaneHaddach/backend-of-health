package com.rickbot.healing_backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.rickbot.healing_backend.mapper.DoctorMapper;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    public DoctorMapper doctorMapper() {
        return mock(DoctorMapper.class);
    }
}
