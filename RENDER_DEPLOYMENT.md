# Render Deployment Guide

This backend is configured for **zero-config deployment** on Render.

## Quick Start

1. **Connect repo to Render**
   - Go to https://dashboard.render.com/
   - Click **New +** → **Blueprint**
   - Select this GitHub repo and branch (`main` or `upgrade/java-25`)
   - Render reads `render.yaml` automatically

2. **Set secrets** (Render prompts during deployment)
   - `JWT_SECRET` — your app's JWT signing key
   - `MYSQL_PASSWORD` — database password (auto-generated if not set)
   - `MYSQL_ROOT_PASSWORD` — MySQL root password (auto-generated if not set)
   - `SPRING_REDIS_PASSWORD` — Redis password (auto-generated if not set)

3. **Deploy**
   - Click **Deploy Blueprint**
   - Render builds the Docker image, deploys services, and runs the app
   - Health check endpoint: `/actuator/health` (auto-monitored)

## What Gets Deployed

| Service | Type | Purpose |
|---------|------|---------|
| `healing-backend` | Web (Docker) | Spring Boot app on port 8080 |
| `healing-db` | MySQL 8.0 | Primary database |
| `healing-redis` | Redis | Cache & session store |

## Environment Variables

All variables are auto-injected from `render.yaml`. Override via Render dashboard if needed:
- `SPRING_PROFILES_ACTIVE=prod` — Production Spring profile
- `SERVER_PORT=8080` — HTTP port
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics,prometheus,info` — Actuator endpoints for monitoring

## Auto-Scaling

- Min instances: 1
- Max instances: 3
- Scales based on CPU/memory usage

## Monitoring

- Health check: `/actuator/health` — checked every 10s
- Metrics: `/actuator/prometheus` available for observability
- Logs: View in Render dashboard → Service → Logs

## Local Testing

Before deploying, test locally:

```bash
./mvnw -B -DskipTests package
docker build -t healing_backend .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/healing_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  healing_backend
```

## Rollback

Push a previous commit to the same branch to trigger a new deploy; Render auto-rolls back on build failure.

---

**Status**: Ready for Render deployment. No additional configuration needed beyond setting secrets.
