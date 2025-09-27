package com.rickbot.healing_backend.dto;

import com.rickbot.healing_backend.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRequestDto {
    
    private String name;
    private String email;
    private String password;
    private String specialization;
    private String phoneNumber;
    private Location location;
    private Double rating;
    private Integer reviewsCount;

}
