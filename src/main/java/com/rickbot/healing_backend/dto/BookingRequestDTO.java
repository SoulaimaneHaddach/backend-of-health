package com.rickbot.healing_backend.dto;

import java.time.LocalDateTime;

public record BookingRequestDTO(
    String phoneNumberUser,
    String emailUser,
    String nameUser,
    LocalDateTime appointmentTime
) {

}
