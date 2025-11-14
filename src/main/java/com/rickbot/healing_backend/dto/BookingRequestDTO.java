package com.rickbot.healing_backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDTO {
    private String phoneNumberUser;
    private String emailUser;
    private String nameUser;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") 
    private LocalDateTime appointmentTime;
}
