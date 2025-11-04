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
import com.rickbot.healing_backend.entity.Book;
import com.rickbot.healing_backend.repository.BookingRepo;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private BookingNotificationController notificationController;

    @InjectMocks
    private BookingService bookingService;

    private Book booking;
    private UUID doctorId;
    private UUID bookingId;

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
    }

    @Test
    void getBookingsByDoctorId_ShouldReturnBookingsList() {
        // Arrange
        List<Book> expectedBookings = Arrays.asList(booking);
        when(bookingRepo.findByDoctorId(doctorId)).thenReturn(expectedBookings);

        // Act
        List<Book> actualBookings = bookingService.getBookingsbyDoctorId(doctorId);

        // Assert
        assertNotNull(actualBookings);
        assertEquals(expectedBookings.size(), actualBookings.size());
        assertEquals(expectedBookings.get(0).getId(), actualBookings.get(0).getId());
        assertEquals(expectedBookings.get(0).getDoctorId(), actualBookings.get(0).getDoctorId());

        // Verify
        verify(bookingRepo).findByDoctorId(doctorId);
    }

    @Test
    void createBooking_ShouldSaveAndNotify() {
        // Arrange
        when(bookingRepo.save(any(Book.class))).thenReturn(booking);
        doNothing().when(notificationController).sendNewBookingNotification(any(Book.class));

        // Act
        Book createdBooking = bookingService.createBooking(booking);

        // Assert
        assertNotNull(createdBooking);
        assertEquals(booking.getId(), createdBooking.getId());
        assertEquals(booking.getDoctorId(), createdBooking.getDoctorId());
        assertEquals(booking.getEmailUser(), createdBooking.getEmailUser());
        assertEquals(booking.getStatus(), createdBooking.getStatus());

        // Verify
        verify(bookingRepo).save(booking);
        verify(notificationController).sendNewBookingNotification(booking);
    }

    @Test
    void updateStatus_WhenBookingExists_ShouldUpdateStatus() {
        // Arrange
        String newStatus = "CONFIRMED";
        when(bookingRepo.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepo.save(any(Book.class))).thenReturn(booking);

        // Act
        Book updatedBooking = bookingService.updateStatus(bookingId, newStatus);

        // Assert
        assertNotNull(updatedBooking);
        assertEquals(newStatus, updatedBooking.getStatus());

        // Verify
        verify(bookingRepo).findById(bookingId);
        verify(bookingRepo).save(booking);
    }

    @Test
    void updateStatus_WhenBookingDoesNotExist_ShouldThrowException() {
        // Arrange
        when(bookingRepo.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
            () -> bookingService.updateStatus(bookingId, "CONFIRMED"),
            "Booking not found");

        // Verify
        verify(bookingRepo).findById(bookingId);
        verify(bookingRepo, never()).save(any());
    }
}