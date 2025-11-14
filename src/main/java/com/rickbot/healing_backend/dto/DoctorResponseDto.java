package com.rickbot.healing_backend.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickbot.healing_backend.entity.Location;
import com.rickbot.healing_backend.entity.WorkingHours;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorResponseDto {
    private UUID id;
    private String email = "";
    private String name = "";
    private String specialization = "";
    private String phoneNumber = "";
    private Location location;
    private Double rating = 0.0;
    private Integer reviewsCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String about = "";
    private List<WorkingHours> hours = new ArrayList<>();
    private List<String> services = new ArrayList<>();
}
