package com.rickbot.healing_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Doctor extends BaseUserEntity {

    private String name;
    private String specialization;
    private String phoneNumber;
    private Location location; // Assuming Location is an @Embeddable or entity
    private Double rating;
    private Integer reviewsCount;
    private String about;

    @ElementCollection
    @CollectionTable(name = "doctor_hours", joinColumns = @JoinColumn(name = "doctor_id"))
    private java.util.List<WorkingHours> hours;

    @ElementCollection
    @CollectionTable(name = "doctor_services", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "service")
    private java.util.List<String> services;
}
