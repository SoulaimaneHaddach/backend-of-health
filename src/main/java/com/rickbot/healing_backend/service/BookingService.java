package com.rickbot.healing_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.entity.Book;
import com.rickbot.healing_backend.repository.BookingRepo;

import jakarta.transaction.Transactional;

import com.rickbot.healing_backend.controller.BookingNotificationController;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepo bookingRepo;
    private final BookingNotificationController notificationController;

    @Transactional
    public List<Book> getBookingsbyDoctorId(UUID doctorId) {
        return bookingRepo.findByDoctorId(doctorId);
    }

    @Transactional
    public Book createBooking(Book booking) {
        
        Book saved = bookingRepo.save(booking);
        // 🔔 Notify all subscribers that a new booking was created
        notificationController.sendNewBookingNotification(saved);

        return saved;
    }

    @Transactional
    public Book updateStatus(UUID bookingId, String status) {
        Book booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(status);
        return bookingRepo.save(booking);
    }

    
}
