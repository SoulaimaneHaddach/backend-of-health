# Railway Quick Start (TL;DR)

## 5-Minute Setup

### 1. Push to GitHub
```bash
git push origin go-back-v1
```

### 2. Railway Setup
- Go to [railway.app](https://railway.app)
- Click **+ New Project** → **Deploy from GitHub repo**
- Select `healing_backend` repository
- Grant Railway access

### 3. Add MySQL Service
```bash
# In Railway dashboard:
# Project → + New → MySQL
# Wait for service to start
```

### 4. Add Redis Service
```bash
# In Railway dashboard:
# Project → + New → Redis
```

### 5. Configure Backend Environment Variables

**Go to:** Backend Service → **Variables**

Paste this (Railway will auto-fill service links):

```
SPRING_PROFILES_ACTIVE=prod
JAVA_TOOL_OPTIONS=-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError

DB_HOST=${{ MySQL.RAILWAY_PRIVATE_HOST }}
DB_PORT=${{ MySQL.RAILWAY_PRIVATE_PORT }}
DB_NAME=healing_db
DB_USER=${{ MySQL.MYSQL_USER }}
DB_PASSWORD=${{ MySQL.MYSQL_PASSWORD }}

SPRING_REDIS_HOST=${{ Redis.RAILWAY_PRIVATE_HOST }}
SPRING_REDIS_PORT=${{ Redis.RAILWAY_PRIVATE_PORT }}
SPRING_REDIS_PASSWORD=${{ Redis.REDIS_PASSWORD }}

JWT_SECRET=<GENERATE_64_CHAR_SECRET_AND_PASTE_HERE>
APP_NOTIFICATIONS_PUSH_ENABLED=true
```

### 6. Initialize Database

**After MySQL starts:**

```bash
# Using Railway CLI
railway shell

# Inside the shell, connect to MySQL:
mysql -h localhost -u $MYSQL_USER -p$MYSQL_PASSWORD

# Then run:
CREATE DATABASE healing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE INDEX idx_doctor_location ON doctor(location_latitude, location_longitude);
CREATE INDEX idx_booking_doctor_id ON book(doctor_id);
```

### 7. Deploy

**Option A: Auto-deploy on Push**
```bash
git push origin go-back-v1
# Railway webhook triggers automatic deployment
```

**Option B: Manual Redeploy**
- Dashboard → Backend Service → **Deployments** → Click latest → **Redeploy**

### 8. Verify Running

- Check Railway Dashboard: All services should show green ✅
- Open Backend Service → **Domain** → Click public URL
- Visit: `https://healing-backend-prod.up.railway.app/actuator/health`
- Should return: `{"status":"UP"}`

### 9. Test Endpoints

```bash
# Replace URL with your Railway domain
BACKEND_URL="https://healing-backend-prod.up.railway.app"

# Health check
curl $BACKEND_URL/actuator/health

# Get doctors (public endpoint)
curl $BACKEND_URL/api/doctors?lat=33.5731&lon=-7.5898&radius=5000

# View metrics (Prometheus format)
curl $BACKEND_URL/actuator/prometheus
```

---

## Environment Mapping

Your app uses these environment variables (Railway auto-provides via service linking):

| Variable | Source | Used By |
|----------|--------|---------|
| `DB_HOST` | `${{ MySQL.RAILWAY_PRIVATE_HOST }}` | application-prod.properties |
| `DB_PORT` | `${{ MySQL.RAILWAY_PRIVATE_PORT }}` | application-prod.properties |
| `DB_USER` | `${{ MySQL.MYSQL_USER }}` | application-prod.properties |
| `DB_PASSWORD` | `${{ MySQL.MYSQL_PASSWORD }}` | application-prod.properties |
| `SPRING_REDIS_HOST` | `${{ Redis.RAILWAY_PRIVATE_HOST }}` | RedisConfig.java |
| `SPRING_REDIS_PORT` | `${{ Redis.RAILWAY_PRIVATE_PORT }}` | RedisConfig.java |
| `SPRING_REDIS_PASSWORD` | `${{ Redis.REDIS_PASSWORD }}` | RedisConfig.java |
| `JWT_SECRET` | Manual (secrets vault) | JwtUtil.java |

---

## Common Issues & Fixes

### Build Fails
```
Check logs:
Dashboard → Backend Service → Deployments → Build Logs

Common causes:
- pom.xml dependency timeout (retry, check internet)
- Java version mismatch (check Dockerfile uses Java 17)
- Dockerfile build error (test locally first)
```

### App Won't Start (Health Check Fails)
```
Check logs:
Dashboard → Backend Service → Logs (Container Logs)

Common causes:
- MySQL not running (check MySQL service status)
- DB credentials wrong (verify ${{ MySQL.MYSQL_USER }}, etc.)
- Redis not running (check Redis service)
- JWT_SECRET missing
```

### Connection Refused Errors
```
Always use PRIVATE hosts for inter-service communication:
✅ CORRECT: DB_HOST=${{ MySQL.RAILWAY_PRIVATE_HOST }}
❌ WRONG: DB_HOST=${{ MySQL.RAILWAY_PUBLIC_HOST }}

Internal services communicate over Railway's private network,
public hosts are only for external connections
```

### Memory/CPU Issues
```
Go to: Backend Service → Settings → Resource Allocation
- Increase Memory (start: 512MB, typical: 1-2GB)
- Increase CPU (start: 0.5, scale: auto up to 2)
- Enable auto-scaling rules
```

---

## Monitoring

### Logs
```
Dashboard → Backend Service → Logs
- Real-time log streaming
- Search by keyword, timestamp, service
```

### Metrics
```
1. Application metrics (Prometheus format):
   GET /actuator/prometheus
   
2. Railway metrics:
   Dashboard → Backend Service → Metrics
   - CPU usage
   - Memory usage
   - Network I/O
   - Request/error rates
```

### Health
```
Health endpoint:
GET /actuator/health
- shows: UP/DOWN status
- component health: MySQL, Redis, etc.
```

---

## Load Testing

```bash
# Install k6
brew install k6

# Run included load test against your Railway URL
k6 run scripts/k6-load-test.js \
  --env BACKEND_URL="https://healing-backend-prod.up.railway.app"

# Results will show:
# - Throughput (requests/sec)
# - Latency (p50, p95, p99)
# - Error rate
```

---

## Scaling

### Horizontal (More Replicas)
```
Backend Service → Deploy Settings
- Min Replicas: 1
- Max Replicas: 3
- CPU Threshold: 70% (scale up), 30% (scale down)

Result: Multiple containers run in parallel, Railway load-balances
```

### Vertical (Bigger Containers)
```
Backend Service → Settings → Resource Allocation
- Memory: 512MB → 2GB
- CPU: 0.5 → 2

Result: Fewer containers, but more powerful (better for CPU-bound tasks)
```

---

## Cost

Ballpark pricing (as of Nov 2024):

| Resource | Cost | Notes |
|----------|------|-------|
| 0.5 vCPU / 512MB RAM | ~$8-15/mo | Development tier |
| 1 vCPU / 1GB RAM | ~$20-30/mo | Production small |
| 2 vCPU / 2GB RAM | ~$40-60/mo | Production medium |
| MySQL 1GB | ~$5-10/mo | With backups |
| Redis 256MB | ~$5-10/mo | Shared tier |
| **Total (small prod)** | ~$40-60/mo | Minimal deployment |
| **Total (medium prod)** | ~$70-100/mo | 2 replicas + monitoring |

See [Railway pricing](https://railway.app/pricing) for latest rates.

---

## Next Steps After Deploy

1. ✅ **Configure Custom Domain** (optional)
   - Get CNAME from Railway dashboard
   - Point your domain registrar to Railway

2. ✅ **Setup Monitoring**
   - Create Grafana dashboards (optional)
   - Configure alerts for error rates, latency

3. ✅ **Enable Database Backups**
   - Railway → MySQL Service → Backups tab
   - Set retention: 30 days minimum

4. ✅ **Update Mobile App**
   - Change backend URL to: `https://healing-backend-prod.up.railway.app`
   - Rebuild and test

5. ✅ **Run Load Tests**
   - Verify performance meets SLAs
   - Collect baseline metrics

---

## Rollback

If something goes wrong:

```
Dashboard → Backend Service → Deployments
↓
Find previous working build
↓
Click "Redeploy"
↓
Health checks verify startup
↓
If still broken, repeat with earlier version
```

---

## Support & Docs

- Railway Docs: https://docs.railway.app
- Community: https://railway.app/support
- This project docs: See `/DEPLOYMENT_CHECKLIST.md`, `/OPTIMIZATION_SUMMARY.md`

---

**Deployment Status: Ready to Go! 🚀**

Your app is production-optimized and Railway-ready.
Push to GitHub and watch the magic happen.
