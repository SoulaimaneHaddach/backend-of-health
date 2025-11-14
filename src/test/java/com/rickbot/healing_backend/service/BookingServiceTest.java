package com.rickbot.healing_backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rickbot.healing_backend.controller.BookingNotificationController;
import com.rickbot.healing_backend.dto.BookingRequestDTO;
import com.rickbot.healing_backend.entity.Book;
import com.rickbot.healing_backend.mapper.BookMapper;
import com.rickbot.healing_backend.repository.BookingRepo;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private BookingNotificationController notificationController;

    @Mock
    private BookMapper bookMapper;  // << FIXED

    @InjectMocks
    private BookingService bookingService;

    private Book booking;
    private UUID doctorId;
    private UUID bookingId;
    private BookingRequestDTO bookingRequest;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        booking = new Book();
        booking.setId(bookingId);
        booking.setDoctorId(doctorId);
        booking.setEmailUser("patient@example.com");
        booking.setPhoneNumberUser("1234567890");
        booking.setNameUser("Test Patient");
        booking.setStatus("PENDING");
        booking.setAppointmentTime(LocalDateTime.now().plusDays(1));

        bookingRequest = new BookingRequestDTO();
        bookingRequest.setEmailUser("patient@example.com");
        bookingRequest.setPhoneNumberUser("1234567890");
        bookingRequest.setNameUser("Test Patient");
        bookingRequest.setAppointmentTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void createBooking_ShouldSaveAndNotify() {
        when(bookMapper.toEntity(any())).thenReturn(booking);
        when(bookingRepo.save(any(Book.class))).thenReturn(booking);
        doNothing().when(notificationController).sendNewBookingNotification(any(Book.class));

        Book createdBooking = bookingService.createBooking(doctorId, bookingRequest);

        assertNotNull(createdBooking);
        verify(bookMapper).toEntity(bookingRequest);
        verify(bookingRepo).save(booking);
        verify(notificationController).sendNewBookingNotification(booking);
    }
}
