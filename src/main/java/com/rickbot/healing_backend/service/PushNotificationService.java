package com.rickbot.healing_backend.service;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.rickbot.healing_backend.entity.DeviceToken;
import com.rickbot.healing_backend.repository.DeviceTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final DeviceTokenRepository deviceTokenRepository;

    @PostConstruct
    void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
                FirebaseApp.initializeApp(options);
                log.info("Initialized FirebaseApp using Application Default Credentials");
            }
        } catch (Exception e) {
            log.warn("Firebase initialization failed or GOOGLE_APPLICATION_CREDENTIALS not set: {}", e.getMessage());
        }
    }

    public void sendNotification(UUID userId, String title, String body, Map<String, String> data) {
        List<DeviceToken> tokens = deviceTokenRepository.findAllByUserId(userId);
        if (tokens == null || tokens.isEmpty()) {
            log.info("No device tokens for user {}", userId);
            return;
        }

        List<String> tokenStrings = tokens.stream().map(DeviceToken::getToken).collect(Collectors.toList());

        MulticastMessage.Builder multicastBuilder = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null) {
            multicastBuilder.putAllData(data);
        }

        multicastBuilder.addAllTokens(tokenStrings);

        MulticastMessage multicast = multicastBuilder.build();

        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            BatchResponse response = messaging.sendMulticast(multicast);
            if (response.getFailureCount() > 0) {
                for (int i = 0; i < response.getResponses().size(); i++) {
                    SendResponse resp = response.getResponses().get(i);
                    if (!resp.isSuccessful()) {
                        String err = resp.getException() != null ? resp.getException().getMessage() : "unknown";
                        String badToken = tokenStrings.get(i);
                        log.warn("Failed to send push to token {}: {}", badToken, err);
                        // Optionally remove tokens that are not registered anymore
                        if (err != null && err.contains("registration-token-not-registered")) {
                            deviceTokenRepository.findByToken(badToken).ifPresent(t -> {
                                deviceTokenRepository.delete(t);
                                log.info("Removed invalid device token {} for user {}", badToken, t.getUserId());
                            });
                        }
                    }
                }
            } else {
                log.info("Sent push to user {}: {} recipients", userId, response.getSuccessCount());
            }
        } catch (FirebaseMessagingException e) {
            log.error("Error sending FCM message: {}", e.getMessage());
        } catch (IllegalStateException ise) {
            log.warn("Firebase not initialized, cannot send push: {}", ise.getMessage());
        }
    }
}
