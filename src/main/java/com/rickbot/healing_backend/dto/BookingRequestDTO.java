package com.rickbot.healing_backend.dto;

import java.time.LocalDateTime;

public record BookingRequestDTO(
    
    String phoneNumber,
    String emaiUser,
    String nameUser,
    LocalDateTime appointmentTime
) {
    

}
