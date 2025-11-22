# Temporary Notification Disable Guide

## Purpose
For development and staging environments, you may want to temporarily disable push notifications without removing the code. This guide shows how.

## Option 1: Disable via Environment Variable (Easiest)

Set the environment variable before running the app:

```bash
# Local development
export APP_NOTIFICATIONS_PUSH_ENABLED=false
./mvnw spring-boot:run

# Docker
docker run -e APP_NOTIFICATIONS_PUSH_ENABLED=false your-image:latest

# Kubernetes
kubectl set env deployment/healing-backend APP_NOTIFICATIONS_PUSH_ENABLED=false
```

## Option 2: Disable via application.properties

Edit `src/main/resources/application.properties` and add:

```properties
app.notifications.push.enabled=false
```

Then rebuild and deploy.

## Option 3: Disable via application-staging.properties (Recommended)

Create `src/main/resources/application-staging.properties`:

```properties
# Staging-specific overrides
app.notifications.push.enabled=false
```

Activate the profile:
```bash
export SPRING_PROFILES_ACTIVE=staging
./mvnw spring-boot:run
```

Or in Docker:
```bash
docker run -e SPRING_PROFILES_ACTIVE=staging your-image:latest
```

## How It Works

A `NotificationsConfig` class in `src/main/java/.../config/` reads the `app.notifications.push.enabled` property. When disabled, the service layer checks this flag before sending push notifications:

```java
@Autowired
private NotificationsConfig notificationsConfig;

public void sendPushNotification(...) {
    if (!notificationsConfig.getPush().isEnabled()) {
        logger.debug("Push notifications disabled; skipping send");
        return;
    }
    // send notification...
}
```

**Effect**: Booking creation, STOMP messages, and all other logic continue normally. Only push notifications are skipped.

## Logs When Disabled

When disabled, you'll see DEBUG logs like:
```
[DEBUG] Push notifications disabled; skipping send
```

## Verifying It's Disabled

1. Create a booking in staging/dev
2. Check logs for "Push notifications disabled" message
3. Verify STOMP message still arrives (WebSocket still works)
4. Verify no push notification is sent to devices

## Re-enabling in Production

For production, ensure `app.notifications.push.enabled=true` (or unset, as default is true):

```properties
# application-prod.properties
app.notifications.push.enabled=true
```

---

**Note**: This is a temporary workaround. For permanent feature flags, consider integrating a feature flag service (LaunchDarkly, Unleash, etc.).
