package com.rickbot.healing_backend.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHours {
    private String day;
    private String openTime;
    private String closeTime;
    private Boolean isClosed;
}
