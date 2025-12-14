# Railway.app Deployment Guide

## Overview
Railway.app is a modern deployment platform that simplifies infrastructure setup, scaling, and monitoring. This guide covers integrating your Healing Backend with Railway's ecosystem.

---

## 1. Prerequisites

- [ ] Railway.app account created ([railway.app](https://railway.app))
- [ ] GitHub account linked to Railway
- [ ] This repository pushed to GitHub
- [ ] Docker image builds successfully locally: `./mvnw clean package && docker build -t healing-backend .`

---

## 2. Architecture Components on Railway

Your app requires these services deployed together:

### Core Services
1. **Java Backend** (Your Spring Boot app) - Compute
2. **MySQL Database** - Data storage
3. **Redis** - Distributed caching & sessions
4. **Prometheus** - Metrics collection (optional but recommended)

### Architecture Diagram
```
┌─────────────────────────────────────────────────────────┐
│                    Railway Environment                  │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Healing Backend (Spring Boot 3.5.7)              │ │
│  │  - Port: 8080                                     │ │
│  │  - Memory: 512MB - 2GB (adjust per load)          │ │
│  │  - CPU: 0.5 - 2 (auto-scale)                      │ │
│  │  - Replicas: 1-3 (configure auto-scaling rules)   │ │
│  │  - Health: /actuator/health                       │ │
│  │  - Metrics: /actuator/prometheus                  │ │
│  └────────────────────────────────────────────────────┘ │
│         ↓                         ↓                      │
│  ┌─────────────────┐   ┌──────────────────────────────┐│
│  │ MySQL (Port     │   │ Redis (Port 6379)            ││
│  │ 3306)           │   │ - Distributed caching        ││
│  │ - Volume: 10GB  │   │ - Session management         ││
│  │ - Backups:      │   │ - Real-time messaging        ││
│  │   enabled       │   │ - Memory: 256MB default      ││
│  └─────────────────┘   └──────────────────────────────┘│
│         ↑ (spring.datasource.*)   ↑ (spring.redis.*)   │
│                                                          │
│  [Optional] Prometheus (Port 9090)                      │
│  - Scrape: :8080/actuator/prometheus                   │
│                                                          │
└─────────────────────────────────────────────────────────┘
                        ↓
                    Railway Domain
                (auto-assigned or custom)
```

---

## 3. Step-by-Step Railway Setup

### 3.1 Create Railway Project

1. Log in to [railway.app](https://railway.app)
2. Click **+ New Project**
3. Select **Deploy from GitHub repo**
4. Choose your `healing_backend` repository
5. Grant Railway access to your GitHub account

### 3.2 Configure Services

#### **A. Spring Boot Backend Service**

1. After repo connection, Railway detects `Dockerfile` → creates backend service automatically
2. **Service Settings:**
   - Name: `healing-backend`
   - Dockerfile: `./Dockerfile` (detected)
   - Port: `8080` (exposed in Dockerfile via EXPOSE)

3. **Environment Variables:**
   Go to Service → **Variables** and add:

   ```
   SPRING_PROFILES_ACTIVE=prod
   JAVA_TOOL_OPTIONS=-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError
   
   # Database (will be linked from MySQL service)
   DB_HOST=${{ MySQL.RAILWAY_PRIVATE_HOST }}
   DB_PORT=${{ MySQL.RAILWAY_PRIVATE_PORT }}
   DB_NAME=healing_db
   DB_USER=${{ MySQL.MYSQL_USER }}
   DB_PASSWORD=${{ MySQL.MYSQL_PASSWORD }}
   
   # Redis (will be linked from Redis service)
   SPRING_REDIS_HOST=${{ Redis.RAILWAY_PRIVATE_HOST }}
   SPRING_REDIS_PORT=${{ Redis.RAILWAY_PRIVATE_PORT }}
   SPRING_REDIS_PASSWORD=${{ Redis.REDIS_PASSWORD }}
   SPRING_REDIS_SSL=false
   
   # Application
   JWT_SECRET=<GENERATE_64_CHAR_SECRET>
   
   # Notifications (optional - set to false for staging)
   APP_NOTIFICATIONS_PUSH_ENABLED=true
   ```

4. **Resource Allocation:**
   - Go to **Deployments** → **Resource**
   - Memory: `1GB` (start), adjust based on monitoring
   - CPU: `0.5` (start), Railway auto-scales
   - Enable auto-restart on failure

5. **Health Checks:**
   - Path: `/actuator/health`
   - Interval: 10s
   - Timeout: 5s

#### **B. MySQL Service**

1. In Railway project, click **+ New** → **MySQL**
2. **MySQL Configuration:**
   - Name: `mysql` (or `healing-db`)
   - Version: Latest (Railway auto-manages)
   - Generated variables:
     - `MYSQL_USER`: Will be available as `${{ MySQL.MYSQL_USER }}`
     - `MYSQL_PASSWORD`: Will be available as `${{ MySQL.MYSQL_PASSWORD }}`
     - `MYSQL_ROOT_PASSWORD`: Auto-generated
     - `MYSQL_DATABASE`: Leave as default, create manually

3. **Post-Connection Setup:**
   After MySQL starts, connect via Railway CLI or GUI to:
   ```sql
   -- Create database (if not auto-created)
   CREATE DATABASE healing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   -- Create initial schema indexes
   CREATE INDEX idx_doctor_location ON doctor(location_latitude, location_longitude);
   CREATE INDEX idx_booking_doctor_id ON book(doctor_id);
   CREATE INDEX idx_booking_user_email ON book(email_user);
   ```

4. **Backups:**
   - Enable automatic backups in Railway dashboard
   - Set retention: 30 days

#### **C. Redis Service**

1. In Railway project, click **+ New** → **Redis**
2. **Redis Configuration:**
   - Name: `redis`
   - Version: Latest
   - Memory: `256MB` (adjust if needed)
   - Eviction policy: `allkeys-lru` (recommended for cache)

3. **Access:**
   - Redis available as `${{ Redis.RAILWAY_PRIVATE_HOST }}`
   - Port: `${{ Redis.RAILWAY_PRIVATE_PORT }}`
   - Password: `${{ Redis.REDIS_PASSWORD }}`

### 3.3 Configure Networking & Domains

1. **Public URL (Domain):**
   - Go to Backend Service → **Domain**
   - Railway generates: `healing-backend-prod.up.railway.app` (or custom domain)
   - HTTPS: Auto-provisioned via Let's Encrypt

2. **Internal Communication:**
   - All services communicate via **private networking** (Railway's internal Docker network)
   - Example: Backend → MySQL uses `${{ MySQL.RAILWAY_PRIVATE_HOST }}`

3. **CORS Configuration:**
   - Update `src/main/java/.../config/SecurityConfig.java`:
   ```java
   cors.setAllowedOriginPatterns(List.of("https://your-frontend-domain.com"));
   ```

---

## 4. Deployment Flow

### Initial Deployment

```
1. Git push to GitHub (branch: main or go-back-v1)
   ↓
2. Railway detects changes (webhook)
   ↓
3. Docker image builds:
   - Stage 1: maven:3.9.6-eclipse-temurin-17 compiles code
   - Stage 2: eclipse-temurin:17-jre runs JAR
   ↓
4. Image deployed to Railway cluster
   ↓
5. Health checks verify startup (/actuator/health)
   ↓
6. Service marked as "Running"
```

### Continuous Deployment

- Every `git push` to your default branch triggers build + deployment
- Configure webhooks in Railway dashboard if needed
- Rollback: Railway keeps previous image versions → click "Redeploy" on earlier version

---

## 5. Environment-Specific Configurations

### Development Profile (Local)
```properties
# src/main/resources/application.properties
spring.profiles.active=dev
spring.cache.type=caffeine
spring.datasource.url=jdbc:mysql://localhost:3306/healing_db
# ... minimal Redis
```

### Production Profile (Railway)
```properties
# src/main/resources/application-prod.properties
spring.profiles.active=prod
spring.cache.type=redis  # Use Redis for distributed caching
spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.redis.host=${SPRING_REDIS_HOST}
# ... see application-prod.properties in repo
```

### Staging Profile (Optional)
Create `src/main/resources/application-staging.properties`:
```properties
# Disable notifications in staging
app.notifications.push.enabled=false
spring.cache.type=caffeine
# Use staging DB / Redis
```

---

## 6. Monitoring & Observability

### 6.1 Railway Dashboard

- **Deployments**: View build logs, deployment history
- **Metrics**: CPU, memory, network I/O (real-time)
- **Logs**: Aggregated application logs with search/filter
- **Environment**: Variables, secrets, linked services

### 6.2 Application Metrics (Prometheus)

1. **Prometheus Endpoint:**
   - Available at: `https://healing-backend-prod.up.railway.app/actuator/prometheus`
   - Includes: JVM metrics, HTTP latency, cache hits/misses, DB pool stats

2. **Optional: Deploy Prometheus on Railway**
   - Add `docker/prometheus.yml` or create via Railway dashboard
   - Scrape backend service: `http://healing-backend:8080/actuator/prometheus`

3. **Optional: Deploy Grafana on Railway**
   - Visualize metrics from Prometheus
   - Dashboards for: JVM, latency, errors, cache, DB

### 6.3 Logging

- Railway aggregates all container logs
- Search by service, time range, text pattern
- Export logs for analysis

---

## 7. Auto-Scaling Configuration

Railway supports horizontal & vertical scaling:

### Horizontal Scaling (Multiple Replicas)
1. Go to Backend Service → **Deploy** tab
2. Set **Min Replicas**: 1, **Max Replicas**: 3
3. Define **Scale Rules**:
   - CPU > 70% → scale up
   - CPU < 30% → scale down

### Vertical Scaling (Larger Memory/CPU)
1. Monitor metrics in Dashboard
2. If consistently high memory usage:
   - Service Settings → Resource → increase memory
3. Redeploy automatically with new resources

---

## 8. Deployment Checklist for Railway

- [ ] Dockerfile builds successfully
- [ ] GitHub repository public or Railway has access
- [ ] MySQL service created & schema initialized
- [ ] Redis service created & configured
- [ ] Backend service environment variables set correctly
- [ ] Health check endpoint working (`/actuator/health`)
- [ ] Public domain assigned (HTTPS enabled)
- [ ] Application starts without errors (check logs)
- [ ] JWT secret generated and stored in Railway secrets
- [ ] Database backups enabled
- [ ] Monitoring dashboards accessible (logs, metrics)
- [ ] CORS policies updated for frontend domain
- [ ] Load test passed with expected throughput (k6 test)

---

## 9. Troubleshooting Common Issues

### Build Fails
```
Error: Maven dependency not found
→ Check internet connectivity in Railway build environment
→ Ensure pom.xml is correct and all repos accessible
→ Check logs: Railway Dashboard → Deployments → Build Logs
```

### Health Check Fails
```
Error: /actuator/health returning 500
→ Check if MySQL is running and accessible
→ Verify DB_HOST, DB_USER, DB_PASSWORD in environment
→ Check app logs for connection errors
```

### Database Connection Timeout
```
Error: com.mysql.cj.jdbc.exceptions.CommunicationsException
→ Ensure MySQL service is running
→ Use private host: ${{ MySQL.RAILWAY_PRIVATE_HOST }}
→ Verify port is 3306 (or Railway's assigned port)
```

### Redis Connection Refused
```
Error: io.lettuce.core.RedisConnectionException
→ Check if Redis service running
→ Use private host: ${{ Redis.RAILWAY_PRIVATE_HOST }}
→ Verify password matches ${{ Redis.REDIS_PASSWORD }}
```

### Memory Limit Exceeded
```
Error: Container killed (OOMKilled)
→ Increase memory allocation in Service Settings
→ Check heap dump for large object allocations
→ Optimize caching strategy or pagination
```

---

## 10. Advanced: Docker Compose Local Testing (Simulating Railway)

To test locally before deploying to Railway:

```bash
# 1. Start local stack matching Railway setup
cd docker/
docker-compose up -d

# 2. Set environment variables
export DB_HOST=localhost
export DB_PORT=3306
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379

# 3. Build and run backend
./mvnw clean package
java -jar target/healing_backend-*.jar

# 4. Test endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/doctors

# 5. Verify metrics
curl http://localhost:8080/actuator/prometheus
```

---

## 11. Post-Deployment Steps

1. **DNS & Domain Configuration:**
   - Point custom domain to Railway's assigned domain (if using custom domain)
   - Enable HSTS if frontend requires it

2. **Frontend Integration:**
   - Update mobile app backend URL to: `https://healing-backend-prod.up.railway.app`
   - Update CORS allowed origins in SecurityConfig

3. **Smoke Testing:**
   - Register a test doctor account
   - Create a test booking
   - Verify WebSocket connection (/ws endpoint)
   - Check push notifications (if enabled)

4. **Performance Baseline:**
   - Run k6 load test: `k6 run scripts/k6-load-test.js`
   - Collect baseline metrics (latency p95, throughput, errors)
   - Set up alerts if metrics degrade

5. **Backup Verification:**
   - Verify MySQL backup accessible in Railway dashboard
   - Test restore procedure in staging first

---

## 12. Useful Railway CLI Commands

```bash
# Install Railway CLI
npm i -g @railway/cli

# Login to Railway
railway login

# Link project
railway link <project-id>

# View logs
railway logs

# Execute remote commands
railway shell
  # Inside shell:
  mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASSWORD

# Deploy
railway deploy

# View environment
railway env
```

---

## 13. Cost Optimization

Railway charges by:
- **Compute**: $0.10 per vCPU-hour
- **Memory**: $0.05 per GB-hour
- **Plugins** (MySQL, Redis): Separate pricing

### Recommendations
- Start with 0.5 vCPU / 512MB memory
- Scale up only if monitoring shows need
- Use shared MySQL/Redis tiers for development
- Enable auto-scaling down to 0 vCPU if idle

---

## 14. Disaster Recovery

### Backup Strategy
- MySQL: Automatic daily backups (retained 30 days)
- Redis: Non-persistent by default (recreate from DB if needed)
- Code: GitHub as single source of truth

### Rollback Procedure
1. Go to Backend Service → **Deployments**
2. Find previous working version
3. Click **Redeploy**
4. Monitor health checks
5. If issues persist, rollback further

### Incident Response
- Railway dashboard → **Support** for infrastructure issues
- Check deployment logs first (Build Logs, Container Logs)
- Verify environment variables haven't changed
- Check MySQL/Redis service status

---

## 15. Next Steps

1. **Deploy to Railway:**
   ```bash
   git push origin go-back-v1  # (or main)
   # Railway webhook triggers automatic deployment
   ```

2. **Monitor:**
   - Watch Railway dashboard for build & deployment status
   - Verify services starting successfully

3. **Test:**
   - Hit public URL endpoints
   - Run k6 load test against public endpoint
   - Verify metrics in Prometheus

4. **Optimize:**
   - Collect JFR profile under load
   - Analyze database slow queries
   - Tune resource allocation based on metrics

---

## Appendix: File Checklist

Ensure these files are in your repo for Railway deployment:

- ✅ `Dockerfile` - Multi-stage build with JVM tuning
- ✅ `pom.xml` - Maven dependencies & versions
- ✅ `src/main/resources/application.properties` - Default config
- ✅ `src/main/resources/application-prod.properties` - Production overrides
- ✅ `.env.example` - Template for environment variables (not deployed)
- ✅ `src/main/java/.../config/RedisConfig.java` - Redis conditional config
- ✅ `src/main/java/.../config/CacheConfig.java` - Caching strategy
- ✅ `docker/prometheus.yml` - Metrics scraping (optional)
- ✅ `DEPLOYMENT_CHECKLIST.md` - Deployment verification

---

**Questions or issues?** 
- Check Railway docs: https://docs.railway.app
- Review logs in Railway dashboard
- Test locally with docker-compose first
