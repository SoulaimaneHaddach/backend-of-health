package com.rickbot.healing_backend.repository;

import com.rickbot.healing_backend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmail(String email);

    org.springframework.data.domain.Page<Admin> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, org.springframework.data.domain.Pageable pageable);
}
