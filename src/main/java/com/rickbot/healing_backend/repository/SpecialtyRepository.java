package com.rickbot.healing_backend.repository;

import com.rickbot.healing_backend.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {
    Optional<Specialty> findByNameIgnoreCase(String name);
}
