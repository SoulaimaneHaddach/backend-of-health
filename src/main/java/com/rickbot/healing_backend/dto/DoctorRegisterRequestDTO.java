package com.rickbot.healing_backend.dto;



public record DoctorRegisterRequestDTO
(
    String name,
    String email,
    String password,
    String specialization,
    String phoneNumber,
    double latitude,
    double longitude,
    String address
) {}
