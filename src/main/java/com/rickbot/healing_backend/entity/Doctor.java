package com.rickbot.healing_backend.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor extends BaseUserEntity {

    private String name;
    private String specialization;
    private String phoneNumber;
    private Location location; // Assuming Location is an @Embeddable or entity
    private Double rating;
    private Integer reviewsCount;
}
