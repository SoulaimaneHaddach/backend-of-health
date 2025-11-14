package com.rickbot.healing_backend.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickbot.healing_backend.entity.Location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorMapDto {
    private UUID id;
    private String email = "";
    private String name = "";
    private String specialization = "";
    private Location location; // latitude, longitude, optional address/city (required, never null)
}
