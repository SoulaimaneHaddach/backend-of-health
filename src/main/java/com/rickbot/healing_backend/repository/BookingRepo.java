package com.rickbot.healing_backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.rickbot.healing_backend.entity.Book;

public interface BookingRepo extends JpaRepository<Book,UUID>{

    List<Book> findByDoctorId(UUID doctorId);
    Page<Book> findByDoctorId(UUID doctorId, Pageable pageable);

    long countByAppointmentTimeBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    
}
