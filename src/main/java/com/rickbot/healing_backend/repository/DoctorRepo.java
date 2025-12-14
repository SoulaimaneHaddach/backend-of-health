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

    @org.springframework.data.jpa.repository.Query(value =
            "SELECT * FROM doctor d WHERE d.location_latitude IS NOT NULL AND d.location_longitude IS NOT NULL " +
            "AND (6371000 * acos( cos( radians(:lat) ) * cos( radians( d.location_latitude ) ) * " +
            "cos( radians( d.location_longitude ) - radians(:lon) ) + sin( radians(:lat) ) * sin( radians( d.location_latitude ) ) ) ) <= :radiusMeters",
            nativeQuery = true)
    java.util.List<Doctor> findDoctorsWithinRadius(@org.springframework.data.repository.query.Param("lat") double lat,
                                                   @org.springframework.data.repository.query.Param("lon") double lon,
                                                   @org.springframework.data.repository.query.Param("radiusMeters") double radiusMeters,
                                                   org.springframework.data.domain.Pageable pageable);
}
