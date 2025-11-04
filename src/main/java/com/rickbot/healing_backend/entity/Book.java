package com.rickbot.healing_backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    @Id
    @UuidGenerator
    private UUID id;
    private String phoneNumberUser;
    private String emailUser;
    private String nameUser;
    private UUID doctorId;
    private String status;
    private LocalDateTime appointmentTime;

}
