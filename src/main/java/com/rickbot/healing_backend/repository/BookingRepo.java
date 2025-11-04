package com.rickbot.healing_backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rickbot.healing_backend.entity.Book;

public interface BookingRepo extends JpaRepository<Book,UUID>{

    List<Book> findByDoctorId(UUID doctorId);

    
}
