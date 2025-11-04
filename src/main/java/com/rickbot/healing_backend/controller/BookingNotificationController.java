package com.rickbot.healing_backend.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.rickbot.healing_backend.entity.Book;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookingNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNewBookingNotification(Book booking) {
        if (booking == null || booking.getDoctorId() == null) {
            return; // Skip notification for invalid bookings
        }

        // Doctor ID from the booking
        String doctorId = booking.getDoctorId().toString();

        // Send the message only to this specific doctor
        messagingTemplate.convertAndSendToUser(
                doctorId,                 // use identifier
                "/queue/bookings",        // destination within user channel
                booking                   // payload (the booking)
        );
    }
}
