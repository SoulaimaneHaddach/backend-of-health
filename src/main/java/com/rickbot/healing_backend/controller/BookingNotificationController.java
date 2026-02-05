package com.rickbot.healing_backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.rickbot.healing_backend.entity.Book;
import com.rickbot.healing_backend.service.PushNotificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookingNotificationController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PushNotificationService pushNotificationService;

    public void sendNewBookingNotification(Book booking) {
        if (booking == null || booking.getDoctorId() == null) {
            return; // Skip notification for invalid bookings
        }

        String doctorId = booking.getDoctorId().toString();

        // Send STOMP message to connected user
        messagingTemplate.convertAndSendToUser(
                doctorId,
                "/queue/bookings",
                booking
        );

        // Also send push notification via FCM
        try {
            Map<String, String> data = new HashMap<>();
            if (booking.getId() != null) data.put("bookingId", booking.getId().toString());
            if (booking.getStatus() != null) data.put("status", booking.getStatus());

            String title = "New booking";
            String body = booking.getNameUser() != null ? booking.getNameUser() + " booked an appointment" : "You have a new booking";

            pushNotificationService.sendNotification(booking.getDoctorId(), title, body, data);
        } catch (Exception e) {
            // Ensure push failures don't impact booking flow
            // Logging is done inside PushNotificationService
        }
    }
}
