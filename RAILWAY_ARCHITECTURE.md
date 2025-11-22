# Railway.app Architecture Checklist

## What You Need to Add to Railway

### ✅ Already Built into Your App

- **Spring Boot 3.5.7** - Production-ready framework
- **Dockerfile** - Multi-stage build with JVM optimization (G1GC, 75% RAM limit)
- **application-prod.properties** - Production configuration with env var substitution
- **RedisConfig.java** - Conditional Redis configuration (auto-activates with SPRING_REDIS_HOST)
- **CacheConfig.java** - Per-cache Caffeine specs (fallback if Redis unavailable)
- **AsyncConfig.java** - Thread pool for async operations (10-50 threads)
- **MetricsConfig.java** - Prometheus metrics export at `/actuator/prometheus`
- **NotificationsConfig.java** - Optional push notification disable via env var
- **LoggingAspect.java** - Debug-guarded logging (no expensive logs in hot paths)
- **DoctorRepo.java** - Native SQL Haversine radius queries (DB-side optimization)
- **Security** - JWT authentication, CORS configured, WebSocket/STOMP support
- **Health Checks** - Built-in via Spring Actuator (`/actuator/health`)

### 🔧 What Railway Will Provide (Infrastructure)

1. **Container Orchestration**
   - Manages Docker image deployment
   - Auto-restarts failed containers
   - Handles load balancing between replicas

2. **Public Domain & HTTPS**
   - Auto-assigns: `healing-backend-prod.up.railway.app`
   - Auto-provisions SSL/TLS via Let's Encrypt
   - Optional custom domain support

3. **Service Networking**
   - Private network between services
   - Auto-DNS resolution: `${{ ServiceName.RAILWAY_PRIVATE_HOST }}`
   - Examples: `${{ MySQL.RAILWAY_PRIVATE_HOST }}`, `${{ Redis.RAILWAY_PRIVATE_HOST }}`

4. **Resource Management**
   - CPU allocation (0.5 - 4 vCPU)
   - Memory allocation (256MB - 16GB)
   - Auto-scaling based on load
   - Persistent volumes for MySQL/Redis data

### 📋 What YOU Need to Add in Railway Dashboard

#### **1. Create MySQL Service**
```
Railway Dashboard → Project → + New → MySQL
Configuration:
  - Name: mysql (or healing-db)
  - Version: Latest (auto-managed)
  - Storage: 10GB (adjust as needed)
  - Backups: Enable (30 day retention)
  
Post-Setup:
  - Create database: healing_db
  - Create indexes on doctor(location_latitude, location_longitude)
```

#### **2. Create Redis Service**
```
Railway Dashboard → Project → + New → Redis
Configuration:
  - Name: redis
  - Version: Latest (auto-managed)
  - Memory: 256MB default (adjust if needed)
  - Eviction policy: allkeys-lru
  - Persistence: Optional (snapshotting)
```

#### **3. Configure Backend Service Environment Variables**
```
Railway Dashboard → Backend Service → Variables

Add these (use dropdown to select service links):
  SPRING_PROFILES_ACTIVE=prod
  JAVA_TOOL_OPTIONS=-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError
  
  [Database - Link to MySQL Service]
  DB_HOST=${{ MySQL.RAILWAY_PRIVATE_HOST }}
  DB_PORT=${{ MySQL.RAILWAY_PRIVATE_PORT }}
  DB_NAME=healing_db
  DB_USER=${{ MySQL.MYSQL_USER }}
  DB_PASSWORD=${{ MySQL.MYSQL_PASSWORD }}
  
  [Redis - Link to Redis Service]
  SPRING_REDIS_HOST=${{ Redis.RAILWAY_PRIVATE_HOST }}
  SPRING_REDIS_PORT=${{ Redis.RAILWAY_PRIVATE_PORT }}
  SPRING_REDIS_PASSWORD=${{ Redis.REDIS_PASSWORD }}
  
  [JWT & Features]
  JWT_SECRET=<GENERATE_64_CHAR_SECRET>
  APP_NOTIFICATIONS_PUSH_ENABLED=true
```

#### **4. Configure Backend Service Deployment Settings**
```
Railway Dashboard → Backend Service → Settings

Deployment Options:
  - Health Check Path: /actuator/health
  - Health Check Interval: 10s
  - Health Check Timeout: 5s
  - Restart Policy: Always
  
Resource Allocation:
  - Memory: 1GB (start), scale up if needed
  - CPU: 0.5 (start), auto-scale up to 2
  - Replicas: 1 (start), scale 1-3 for HA
  
Auto-Scaling Rules (optional):
  - CPU > 70% → add 1 replica
  - CPU < 30% → remove 1 replica
```

#### **5. Configure Public Domain**
```
Railway Dashboard → Backend Service → Domain

Options:
  a) Use Railway's default domain: healing-backend-prod.up.railway.app
  b) Use custom domain: api.yourdomain.com
     - Get CNAME from Railway
     - Point in your domain registrar

HTTPS: Auto-provisioned (Let's Encrypt)
```

### 🚀 Deployment Flow Diagram

```
┌──────────────────────────────────────┐
│  Your Local Development              │
│  - Spring Boot app                   │
│  - MySQL locally (docker-compose)    │
│  - Redis locally (docker-compose)    │
│  - GitHub repo (go-back-v1 branch)   │
└──────────────────────────────────────┘
               │
               │ git push
               ↓
┌──────────────────────────────────────┐
│  GitHub Repository                   │
│  - Webhook triggers Railway           │
└──────────────────────────────────────┘
               │
               │ webhook
               ↓
┌──────────────────────────────────────┐
│  Railway Build Environment            │
│  - Pulls Dockerfile                  │
│  - Stage 1: Maven build              │
│  - Stage 2: JRE runtime              │
│  - Creates image                     │
└──────────────────────────────────────┘
               │
               │ image push
               ↓
┌──────────────────────────────────────┐
│  Railway Production Environment       │
│  ┌──────────────────────────────────┐│
│  │ Backend Container                ││
│  │ - Spring Boot 3.5.7              ││
│  │ - Port 8080                      ││
│  │ - G1GC with 75% RAM              ││
│  │ - Health endpoint: /actuator/... ││
│  │ - Metrics: /actuator/prometheus  ││
│  └──────────────────────────────────┘│
│         ↓                ↓              │
│  ┌────────────┐   ┌──────────────┐   │
│  │ MySQL      │   │ Redis        │   │
│  │ Private IP │   │ Private IP   │   │
│  │ Port 3306  │   │ Port 6379    │   │
│  └────────────┘   └──────────────┘   │
│                                       │
│  Public URL (HTTPS):                 │
│  healing-backend-prod.up.railway.app │
└──────────────────────────────────────┘
               ↓
       (External requests)
               ↓
        Mobile App / Frontend
```

### 📊 Service Linking in Railway

When you link services in Railway, it auto-generates connection variables:

**MySQL Service Provides:**
```
${{ MySQL.MYSQL_USER }}           → database username
${{ MySQL.MYSQL_PASSWORD }}       → database password
${{ MySQL.MYSQL_DATABASE }}       → database name
${{ MySQL.RAILWAY_PRIVATE_HOST }} → internal hostname (use this)
${{ MySQL.RAILWAY_PUBLIC_HOST }}  → external hostname (don't use for backend)
${{ MySQL.RAILWAY_PRIVATE_PORT }} → internal port
${{ MySQL.RAILWAY_PUBLIC_PORT }}  → external port (don't use for backend)
```

**Redis Service Provides:**
```
${{ Redis.REDIS_PASSWORD }}       → authentication password
${{ Redis.RAILWAY_PRIVATE_HOST }} → internal hostname (use this)
${{ Redis.RAILWAY_PUBLIC_HOST }}  → external hostname (don't use for backend)
${{ Redis.RAILWAY_PRIVATE_PORT }} → internal port
${{ Redis.RAILWAY_PUBLIC_PORT }}  → external port (don't use for backend)
```

### 🛠️ Configuration Files Ready

Your repo has everything needed:

```
healing_backend/
├── Dockerfile                          ✅ Multi-stage, JVM optimized
├── pom.xml                            ✅ Dependencies locked
├── src/main/resources/
│   ├── application.properties          ✅ Defaults (dev)
│   └── application-prod.properties     ✅ Production overrides (Railway)
├── src/main/java/.../config/
│   ├── RedisConfig.java               ✅ Redis (conditional on env var)
│   ├── CacheConfig.java               ✅ Caching strategy
│   ├── AsyncConfig.java               ✅ Thread pools
│   ├── MetricsConfig.java             ✅ Prometheus export
│   └── NotificationsConfig.java       ✅ Notification toggle
├── .railway.env                        ✅ Environment variable template
├── RAILWAY_DEPLOYMENT_GUIDE.md        ✅ Complete setup guide
├── RAILWAY_QUICKSTART.md              ✅ 5-minute quick start
├── DEPLOYMENT_CHECKLIST.md            ✅ Verification steps
└── docker/prometheus.yml              ✅ Metrics scraping (optional)
```

### 🔗 Data Flow: How It Works on Railway

**1. Request Comes In:**
```
Customer → HTTPS (Railway handles) → healing-backend-prod.up.railway.app:443
                                     ↓ (internal routing)
                                     Backend Container:8080
```

**2. Backend Needs Data:**
```
Backend Container
  ↓ (uses DB_HOST=${{ MySQL.RAILWAY_PRIVATE_HOST }})
  ↓ (Railway private network DNS resolution)
  MySQL Container (port 3306)
```

**3. Cache Lookup:**
```
Backend Container
  ↓ (uses SPRING_REDIS_HOST=${{ Redis.RAILWAY_PRIVATE_HOST }})
  ↓ (Redis configured, so uses it; if down, falls back to Caffeine)
  Redis Container (port 6379)
```

**4. Response Sent Back:**
```
Backend Container → Response JSON
                    ↓ (HTTPS compression enabled)
                    ↓ (Railway handles SSL/compression)
                    → Customer (gzip compressed)
```

### ✨ What's Optimized Already

- **CPU**: G1GC, -XX:MaxRAMPercentage=75% (uses available memory efficiently)
- **Memory**: Async logging (no thread blocking), Redis caching (distributed), Caffeine fallback (in-process)
- **Response Time**: Native SQL queries (DB-side), async endpoints, caching
- **Reliability**: Health checks, graceful shutdown, heap dumps on OOM
- **Observability**: Prometheus metrics, structured logging, async appenders

### 🎯 Summary: What to Do

**Step 1: Prepare**
```bash
git push origin go-back-v1
```

**Step 2: Railway Setup**
- Create MySQL service
- Create Redis service
- Link services to backend
- Set environment variables (use Railway service linking syntax)

**Step 3: Deploy**
- Railway auto-deploys on GitHub webhook
- Watch logs in Railway dashboard
- Verify health check passes

**Step 4: Test**
```bash
curl https://healing-backend-prod.up.railway.app/actuator/health
curl https://healing-backend-prod.up.railway.app/api/doctors
```

**Step 5: Monitor**
- Railway dashboard: CPU, memory, requests
- Application metrics: `/actuator/prometheus`
- Logs: Real-time in Railway dashboard

---

## Architecture Summary Table

| Component | Local (docker-compose) | Railway | Notes |
|-----------|------------------------|---------|-------|
| Backend | Port 8080 | Port 8080 (internal) | Same, Railway adds domain & HTTPS |
| MySQL | localhost:3306 | MySQL service (private IP) | Automatic backups on Railway |
| Redis | localhost:6379 | Redis service (private IP) | Auto-provisioned, managed |
| Monitoring | Prometheus:9090 | Railway dashboard + Prometheus | Optional external Prometheus |
| Logging | Console + File | Railway logs + ELK (optional) | Aggregated in Railway dashboard |
| Domain | localhost | healing-backend-prod.up.railway.app | Auto HTTPS, optional custom domain |
| SSL/TLS | None | Let's Encrypt (auto-renewed) | Railway handles certification |

---

**You're Ready! 🚀**

Everything is configured and optimized for Railway.app deployment.
Push to GitHub and Railway will handle the rest automatically.

See `RAILWAY_QUICKSTART.md` for the 5-minute setup walkthrough.
