package com.rickbot.healing_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter  @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Doctor extends BaseUserEntity {
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String phoneNumber;

    @Embedded
    private Location location;

    private Double rating;
    private Integer reviewsCount;
    


}
