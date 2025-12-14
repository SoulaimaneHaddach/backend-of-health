package com.rickbot.healing_backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rickbot.healing_backend.dto.BookingRequestDTO;
import com.rickbot.healing_backend.entity.Book;
import com.rickbot.healing_backend.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    // GET /api/doctors/{doctorId}/bookings
    @GetMapping("/doctors/{doctorId}/bookings")
    public List<Book> getBookings(@PathVariable UUID doctorId) {
        return bookingService.getBookingsbyDoctorId(doctorId);
    }

    // POST /api/doctors/{doctorId}/bookings
    

    @PostMapping("/doctors/{doctorId}/bookings")
    public ResponseEntity<Book> createBooking(@PathVariable("doctorId") UUID doctorId,
                              @RequestBody BookingRequestDTO bookingRequest) {
        
        Book saved = bookingService.createBooking(doctorId,bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/bookings/{bookingId}/status?status=ACCEPTED
    @PutMapping("/bookings/{bookingId}/status")
    public Book updateBookingStatus(@PathVariable UUID bookingId,
                                    @RequestParam String status) {
        return bookingService.updateStatus(bookingId, status);
    }
}
