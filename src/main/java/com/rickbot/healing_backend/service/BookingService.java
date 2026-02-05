package com.rickbot.healing_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.entity.Book;
import com.rickbot.healing_backend.mapper.BookMapper;
import com.rickbot.healing_backend.repository.BookingRepo;

import jakarta.transaction.Transactional;

import com.rickbot.healing_backend.controller.BookingNotificationController;
import com.rickbot.healing_backend.dto.BookingRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService { 
    
    private final BookingRepo bookingRepo;
    private final BookingNotificationController notificationController;
    private final BookMapper bookMapper;

    public static final String DEFAULT_STATUS = "PENDING";

    @Transactional
    public List<Book> getBookingsbyDoctorId(UUID doctorId, int page, int limit) {
        var pg = org.springframework.data.domain.PageRequest.of(Math.max(0, page - 1), Math.max(1, limit));
        return bookingRepo.findByDoctorId(doctorId, pg).getContent();
    }

    @Transactional
    public List<Book> getAllBookings() {
        return bookingRepo.findAll();
    }

    @Transactional
    public Optional<Book> findById(UUID id) {
        return bookingRepo.findById(id);
    }

    @Transactional
    public Book createBooking(Book booking) {
        Book saved = bookingRepo.save(booking);
        notificationController.sendNewBookingNotification(saved);
        return saved;
    }

    @Transactional
    public Book createBooking(UUID doctorId, BookingRequestDTO bookingRequest) {
        Book booking = bookMapper.toEntity(bookingRequest);
        booking.setDoctorId(doctorId);
        if (booking.getStatus() == null || booking.getStatus().isBlank()) {
            booking.setStatus(DEFAULT_STATUS);
        }
        Book saved = bookingRepo.save(booking);
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

    @Transactional
    public Book updateBooking(UUID bookingId, String status, String notes) {
        Book booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (status != null && !status.isBlank()) booking.setStatus(status);
        if (notes != null) booking.setNotes(notes);
        return bookingRepo.save(booking);
    }

    @Transactional
    public Book confirmBooking(UUID bookingId) {
        return updateBooking(bookingId, "ACCEPTED", null);
    }

    @Transactional
    public Book cancelBooking(UUID bookingId, String reason) {
        Book booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("CANCELLED");
        booking.setCancelReason(reason);
        return bookingRepo.save(booking);
    }
}
