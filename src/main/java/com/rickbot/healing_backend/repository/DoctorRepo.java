package com.rickbot.healing_backend.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


import com.rickbot.healing_backend.entity.Doctor;

public interface DoctorRepo extends JpaRepository<Doctor, UUID>{

    List<Doctor> findBySpecializationIgnoreCase(String specialization);
    List<Doctor> findBySpecializationIgnoreCaseAndRatingGreaterThanEqual(String specialization, Double minRating);
    List<Doctor> findByNameContainingIgnoreCase(String namePart);
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findById(UUID id);
}
