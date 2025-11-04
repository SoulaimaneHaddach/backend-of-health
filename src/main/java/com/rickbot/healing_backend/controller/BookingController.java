package com.rickbot.healing_backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.dto.BookingRequestDTO;
import com.rickbot.healing_backend.entity.Book;
import com.rickbot.healing_backend.mapper.BookMapper;
import com.rickbot.healing_backend.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BookingController {
    

    private final BookingService bookingService;
    private final BookMapper bookMapper;
    

    @GetMapping("/api/bookings/")
    public List<Book> getBookings(UUID doctorId) {
        return bookingService.getBookingsbyDoctorId(doctorId);
    }

    @PostMapping("/api/bookings/")
    public Book createBooking(UUID doctorid,BookingRequestDTO bookingRequest) {
        Book booking = bookMapper.toEntity(bookingRequest);
        return bookingService.createBooking(booking);
    }

    @PutMapping("/api/bookings/status")
    public Book updateBookingStatus(UUID bookingId, String status) {
        return bookingService.updateStatus(bookingId, status);
    }
}
