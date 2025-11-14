package com.rickbot.healing_backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record BookingRequestDTO(
    String phoneNumberUser,
    String emailUser,
    String nameUser,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime appointmentTime
) {

}
