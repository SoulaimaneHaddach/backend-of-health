# Backend Spec: Mobile Booking & Notifications Integration

**Purpose**: Enable mobile app to receive real-time booking notifications and reconcile offline-created bookings.

---

## Required Endpoints

### 1. POST /devices
**Auth**: Bearer JWT  
**Request**:
```json
{
  "token": "<devicePushToken>",
  "platform": "ios|android",
  "tokenType": "native|expo",
  "metadata": { "appVersion": "1.0.0", "locale": "en" }
}
```
**Behavior**: Upsert device by token, associate with authenticated user.  
**Response**: 201 Created
```json
{
  "id": "device-uuid",
  "userId": "user-uuid",
  "token": "...",
  "platform": "android",
  "tokenType": "native",
  "createdAt": "2025-11-22T10:00:00Z"
}
```

---

### 2. POST /bookings
**Auth**: Bearer JWT  
**Request**:
```json
{
  "clinicId": "c1",
  "doctorId": "d1",
  "time": "2025-11-20T15:30:00Z",
  "patient": { "name": "John", "phone": "..." },
  "clientLocalId": "local-123"
}
```
**Behavior**:
- Create booking, persist optional `clientLocalId` linked to booking.
- **Idempotency**: If same user supplies same `clientLocalId`, return existing booking (200) instead of duplicate.

**Response**: 201 Created (or 200 if duplicate)
```json
{
  "id": "booking-uuid",
  "clientLocalId": "local-123",
  "status": "confirmed",
  "time": "2025-11-20T15:30:00Z",
  "doctorId": "d1",
  "clinicId": "c1",
  "patient": { "name": "John", "phone": "..." }
}
```

---

### 3. GET /bookings
**Auth**: Bearer JWT  
**Query**: `?since=<timestamp>` (optional, for incremental polling)  
**Behavior**: Return paginated bookings for authenticated user.  
**Response**: 200 OK
```json
{
  "bookings": [
    { "id": "...", "clientLocalId": "...", "status": "confirmed", "time": "..." },
    ...
  ],
  "total": 10,
  "page": 0,
  "pageSize": 20
}
```

---

## WebSocket / STOMP (Real-Time Notifications)

**WSS Endpoint**: `wss://{host}/ws`  
**STOMP Destination**: `/user/{userId}/queue/bookings`

### Connection Authentication
- Validate JWT during WS connect (token as query param `?token=<jwt>` or in STOMP CONNECT headers).
- Map connection → authenticated `userId`.

### Message Format (on Booking Creation)
Publish STOMP message to `/user/{userId}/queue/bookings`:
```json
{
  "type": "booking:created",
  "booking": {
    "id": "booking-uuid",
    "status": "confirmed",
    "time": "2025-11-20T15:30:00Z",
    "doctorId": "d1",
    "clinicId": "c1"
  },
  "clientLocalId": "local-123"
}
```

### Security
- Subscriptions to `/user/{X}` allowed only for authenticated user matching `{X}`.
- Reject cross-user subscription attempts.

---

## Push Notifications

**Trigger**: On booking creation.  
**Recipients**: All devices registered via `POST /devices` for the user.

### Notification Payload
```json
{
  "title": "New booking",
  "body": "You have a new booking",
  "data": {
    "screen": "DoctorBookings",
    "bookingId": "booking-uuid",
    "clientLocalId": "local-123"
  }
}
```

**Providers**:
- Native Android: FCM (Firebase Cloud Messaging)
- iOS: APNs (Apple Push Notification service)
- Expo: Expo Push API

---

## Reverse Proxy / TLS Configuration

**nginx/ALB WebSocket Support**:
```nginx
location /ws {
  proxy_pass http://backend:8080;
  proxy_http_version 1.1;
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection "upgrade";
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  proxy_set_header X-Forwarded-Proto $scheme;
}
```

**Requirements**:
- Valid TLS certs for WSS (wss://)
- If using dev tunnel (ngrok, etc.), ensure WebSocket upgrades supported

---

## Logging & Metrics

- Log WS handshake failures, close codes/reasons (include timestamp, client IP)
- Expose lightweight `GET /ws-health` → 200 if WS upgrades accepted
- Track metrics: active WS connections, bookings created, notifications sent

---

## Minimal Test Flow

1. **Connect to WS**:
   ```bash
   wscat -c "wss://host/ws?token=<jwt>"
   ```

2. **Register device**:
   ```bash
   curl -X POST http://host/devices \
     -H "Authorization: Bearer <jwt>" \
     -H "Content-Type: application/json" \
     -d '{"token":"abc123","platform":"android","tokenType":"native"}'
   ```

3. **Create booking with clientLocalId**:
   ```bash
   curl -X POST http://host/bookings \
     -H "Authorization: Bearer <jwt>" \
     -H "Content-Type: application/json" \
     -d '{
       "clinicId":"c1","doctorId":"d1","time":"2025-11-20T15:30:00Z",
       "patient":{"name":"John","phone":"555-1234"},
       "clientLocalId":"local-123"
     }'
   ```
   Expected: HTTP 201 with booking + echoed `clientLocalId`

4. **Verify STOMP message**:
   - Connected WS client should receive STOMP message with booking and `clientLocalId`
   - Payload can be printed in wscat or app console

5. **Verify push notification**:
   - Check device's notification center (iOS/Android simulator or real device)
   - Notification payload includes `bookingId` and `clientLocalId` for app reconciliation

---

## Implementation Checklist

- [ ] Device management: `POST /devices` with upsert logic
- [ ] Booking creation: `POST /bookings` with idempotency on `clientLocalId`
- [ ] Booking fetch: `GET /bookings` with optional `since` query
- [ ] WebSocket: STOMP broker, JWT auth in connect phase
- [ ] STOMP messaging: publish to `/user/{userId}/queue/bookings` on booking creation
- [ ] Push notifications: FCM/APNs/Expo integration with `data` payload
- [ ] Reverse proxy: WebSocket Upgrade headers configured
- [ ] Logging: WS failures, close codes, notification send results
- [ ] Metrics: active connections, bookings, notifications
- [ ] Tests: integration tests for end-to-end flow

---

## Optional: Disable Notifications Temporarily (Dev/Staging)

Set environment variable to skip push notifications:
```bash
DISABLE_PUSH_NOTIFICATIONS=true
```

Or via `application.properties`:
```properties
app.notifications.push.enabled=false
```

When disabled, bookings and STOMP messages still work; push notifications are silently skipped (logged at DEBUG level).

---

**Status**: Ready for backend dev implementation. Coordinate WebSocket upgrade testing with DevOps/platform team.
