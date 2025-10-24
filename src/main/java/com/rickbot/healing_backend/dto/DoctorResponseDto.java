package com.rickbot.healing_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.rickbot.healing_backend.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponseDto {
    private UUID id;
    private String email;
    private String name;
    private String specialization;
    private String phoneNumber;
    private Location location;
    private Double rating;
    private Integer reviewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
