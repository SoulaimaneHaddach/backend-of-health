package com.rickbot.healing_backend.dto;

import java.util.UUID;

import com.rickbot.healing_backend.entity.Location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMapDto {
    private UUID id;
    private String email;
    private String name;
    private String specialization;
    private Location location; // latitude, longitude, optional address/city
}
