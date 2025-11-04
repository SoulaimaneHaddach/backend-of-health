package com.rickbot.healing_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.rickbot.healing_backend.entity.Book;

@ExtendWith(MockitoExtension.class)
public class BookingNotificationControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BookingNotificationController notificationController;

    private Book booking;
    private UUID doctorId;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        
        booking = new Book();
        booking.setId(UUID.randomUUID());
        booking.setDoctorId(doctorId);
        booking.setEmailUser("patient@example.com");
        booking.setPhoneNumberUser("1234567890");
        booking.setNameUser("Test Patient");
        booking.setStatus("PENDING");
        booking.setAppointmentTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void sendNewBookingNotification_ShouldSendToCorrectDestination() {
        // Arrange
        String expectedDestination = "/queue/bookings";

        // Act
        notificationController.sendNewBookingNotification(booking);

        // Assert & Verify
        verify(messagingTemplate).convertAndSendToUser(
            eq(doctorId.toString()),    // verify correct doctor ID
            eq(expectedDestination),    // verify correct destination
            eq(booking)                 // verify correct payload
        );
    }

    @Test
    void sendNewBookingNotification_WithNullBooking_ShouldHandleGracefully() {
        // Arrange
        Book nullBooking = null;

        // Act & Assert
        try {
            notificationController.sendNewBookingNotification(nullBooking);
        } catch (Exception e) {
            // Test should fail if any exception is thrown
            throw new AssertionError("Should handle null booking gracefully", e);
        }
    }

    @Test
    void sendNewBookingNotification_ShouldFormatMessageCorrectly() {
        // Arrange
        String expectedUserId = doctorId.toString();
        String expectedDestination = "/queue/bookings";

        // Act
        notificationController.sendNewBookingNotification(booking);

        // Verify that message is sent with correct format
        verify(messagingTemplate).convertAndSendToUser(
            eq(expectedUserId),
            eq(expectedDestination),
            any(Book.class)
        );
    }
}