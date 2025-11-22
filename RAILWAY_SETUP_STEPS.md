# Railway Configuration Step-by-Step

## 🎯 Exact Steps to Deploy on Railway.app

---

## Phase 1: Repository Preparation

### Step 1.1: Push Code to GitHub
```bash
cd /Users/garp/Documents/predeploy/healing_backend

# Verify branch
git branch -a

# Push your optimized code
git push origin go-back-v1

# Or push to main if that's your default
git push origin main
```

### Step 1.2: Verify Files Exist
These files are required for Railway deployment:

```bash
# Check essential files
ls -la Dockerfile                          # ✅ Must exist
ls -la pom.xml                            # ✅ Must exist
ls -la src/main/resources/application-prod.properties  # ✅ Must exist
ls -la src/main/java/com/rickbot/healing_backend/config/RedisConfig.java  # ✅ For Redis
```

---

## Phase 2: Railway Project Creation

### Step 2.1: Create Railway Account & Project
1. Go to [railway.app](https://railway.app)
2. Sign up / Log in (GitHub OAuth recommended)
3. Click **+ New Project**
4. Select **Deploy from GitHub repo**
5. Authorize Railway to access GitHub
6. Search & select `healing_backend` repository
7. Select branch: `go-back-v1` (or `main` if that's your default)

### Step 2.2: Railway Auto-Detects Dockerfile
- Railway will automatically detect `Dockerfile`
- Backend service will be created: `healing-backend` (or auto-named)
- Click **Deploy** to trigger initial build

**Status to watch:**
```
Deployment Status: Building... → Waiting for service... → Running
```

---

## Phase 3: Add MySQL Service

### Step 3.1: Add MySQL via Railway Dashboard
1. Go to Your Railway Project
2. Click **+ New** button (bottom of service list)
3. Search for and click **MySQL**
4. Railway auto-provisions:
   - Database: `railway` (default)
   - User: `root` (auto-generated)
   - Port: `3306`
   - Storage: 10GB

### Step 3.2: Get MySQL Credentials
```
In Railway Dashboard:
  MySQL Service → Variables tab → Copy these:
    MYSQL_USER
    MYSQL_PASSWORD
    MYSQL_ROOT_PASSWORD
    RAILWAY_PRIVATE_HOST
    RAILWAY_PRIVATE_PORT
```

### Step 3.3: Initialize Database
**Option A: Using Railway CLI**
```bash
# Install CLI
npm install -g @railway/cli

# Login
railway login

# Connect to project
railway link <your-project-id>

# Open shell into MySQL
railway shell --service mysql

# Inside the shell
mysql> CREATE DATABASE healing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
mysql> USE healing_db;
mysql> CREATE INDEX idx_doctor_location ON doctor(location_latitude, location_longitude);
mysql> CREATE INDEX idx_booking_doctor_id ON book(doctor_id);
mysql> EXIT;
```

**Option B: Manual via Connection String**
```bash
# Get connection string from Railway MySQL service
# Example: mysql://user:pass@host.railway.app:port/railway

mysql -h <RAILWAY_HOST> -u <MYSQL_USER> -p<MYSQL_PASSWORD> -P <PORT>
# Then run the CREATE DATABASE and INDEX statements above
```

---

## Phase 4: Add Redis Service

### Step 4.1: Add Redis via Railway Dashboard
1. Go to Your Railway Project
2. Click **+ New** button
3. Search for and click **Redis**
4. Railway auto-provisions:
   - Memory: 256MB (default)
   - Port: `6379`
   - Eviction Policy: `noeviction` (default)

### Step 4.2: Get Redis Credentials
```
In Railway Dashboard:
  Redis Service → Variables tab → Copy these:
    REDIS_PASSWORD
    RAILWAY_PRIVATE_HOST
    RAILWAY_PRIVATE_PORT
```

### Step 4.3: Verify Redis Connection (Optional)
```bash
# Using redis-cli (if installed)
redis-cli -h <RAILWAY_REDIS_HOST> -p <RAILWAY_REDIS_PORT> -a <REDIS_PASSWORD>
> PING
PONG
> EXIT
```

---

## Phase 5: Configure Backend Environment Variables

### Step 5.1: Add Variables to Backend Service
1. Go to Railway Dashboard → Your Project → **Backend Service**
2. Click **Variables** tab
3. Add each variable by clicking **+ New Variable**

### Step 5.2: Database Configuration Variables

**Copy-paste exactly (Railway will auto-fill service links):**

```
Name: SPRING_PROFILES_ACTIVE
Value: prod

Name: JAVA_TOOL_OPTIONS
Value: -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError -XX:+ExitOnOutOfMemoryError

Name: DB_HOST
Value: ${{ MySQL.RAILWAY_PRIVATE_HOST }}

Name: DB_PORT
Value: ${{ MySQL.RAILWAY_PRIVATE_PORT }}

Name: DB_NAME
Value: healing_db

Name: DB_USER
Value: ${{ MySQL.MYSQL_USER }}

Name: DB_PASSWORD
Value: ${{ MySQL.MYSQL_PASSWORD }}
```

### Step 5.3: Redis Configuration Variables

```
Name: SPRING_REDIS_HOST
Value: ${{ Redis.RAILWAY_PRIVATE_HOST }}

Name: SPRING_REDIS_PORT
Value: ${{ Redis.RAILWAY_PRIVATE_PORT }}

Name: SPRING_REDIS_PASSWORD
Value: ${{ Redis.REDIS_PASSWORD }}

Name: SPRING_REDIS_SSL
Value: false
```

### Step 5.4: JWT Secret

**Generate a strong JWT secret:**
```bash
# On your local machine
openssl rand -base64 32 | tr -d '\n' && echo

# Output example:
# AbCdEfGhIjKlMnOpQrStUvWxYz1234567890ABCDEFGhijklmn
```

**Add to Railway:**
```
Name: JWT_SECRET
Value: <PASTE_GENERATED_SECRET_HERE>
```

### Step 5.5: Application Features

```
Name: APP_NOTIFICATIONS_PUSH_ENABLED
Value: true

Name: SPRING_APPLICATION_NAME
Value: healing_backend

Name: SERVER_PORT
Value: 8080

Name: SERVER_COMPRESSION_ENABLED
Value: true
```

### Step 5.6: Logging Configuration (Optional)

```
Name: LOGGING_LEVEL_ROOT
Value: WARN

Name: LOGGING_LEVEL_COM_RICKBOT_HEALING_BACKEND
Value: INFO

Name: LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB
Value: WARN
```

---

## Phase 6: Deploy & Verify

### Step 6.1: Trigger Deployment

**Option A: Auto-deploy on Git Push**
```bash
# Any git push to your branch triggers deployment
cd /Users/garp/Documents/predeploy/healing_backend
git add .
git commit -m "Add Railway configuration"
git push origin go-back-v1

# Railway webhook detects push and auto-deploys
```

**Option B: Manual Redeploy**
1. Railway Dashboard → Backend Service
2. Click **Deployments** tab
3. Find latest build → Click **Redeploy**

### Step 6.2: Monitor Deployment Progress

```
Timeline:
  0s    → Building (Maven compile)
  30-60s → Pushing image to Railway registry
  90s   → Starting container
  120s  → Health check running
  150s  → Deployment complete ✅
```

**Watch in Railway Dashboard:**
- Deployments tab → Status: Building... → Deployment Logs
- Look for: `BUILD SUCCESS` and `Application started`

### Step 6.3: Verify All Services Running

```
Railway Dashboard:
  ✅ Backend Service: Running (green dot)
  ✅ MySQL Service: Running (green dot)
  ✅ Redis Service: Running (green dot)
```

---

## Phase 7: Test & Verify

### Step 7.1: Get Public URL

```
Railway Dashboard → Backend Service → Domain tab
Copy: https://healing-backend-prod.up.railway.app
(Or your custom domain if configured)
```

### Step 7.2: Test Health Endpoint

```bash
# Replace with your Railway domain
curl https://healing-backend-prod.up.railway.app/actuator/health

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
```

If not UP, check logs:
```bash
Railway Dashboard → Backend Service → Logs
Look for errors related to:
  - Database connection
  - Redis connection
  - Application startup
```

### Step 7.3: Test API Endpoints

```bash
# Get doctors
curl "https://healing-backend-prod.up.railway.app/api/doctors?lat=33.5731&lon=-7.5898&radius=5000"

# Get metrics (Prometheus format)
curl "https://healing-backend-prod.up.railway.app/actuator/prometheus" | head -20

# Application info
curl "https://healing-backend-prod.up.railway.app/actuator/info"
```

### Step 7.4: View Logs in Real-Time

```bash
# Option 1: Railway Dashboard
Dashboard → Backend Service → Logs tab (auto-refreshes)

# Option 2: Railway CLI
railway logs --follow --service healing-backend
```

---

## Phase 8: Configure Custom Domain (Optional)

### Step 8.1: Add Custom Domain
1. Railway Dashboard → Backend Service → **Domain** tab
2. Click **+ Add Domain**
3. Enter your domain: `api.yourdomain.com`
4. Railway generates CNAME:
   ```
   api.yourdomain.com CNAME healing-backend-prod.up.railway.app
   ```

### Step 8.2: Point Domain in Registrar
1. Go to your domain registrar (GoDaddy, Namecheap, Route53, etc.)
2. Add CNAME record:
   ```
   Host: api
   Type: CNAME
   Value: healing-backend-prod.up.railway.app
   ```
3. Wait for DNS propagation (5-48 hours)
4. Test:
   ```bash
   curl https://api.yourdomain.com/actuator/health
   ```

### Step 8.3: Update CORS in Code (If Using Custom Domain)
```java
// src/main/java/.../config/SecurityConfig.java
cors.setAllowedOriginPatterns(List.of(
    "https://yourdomain.com",
    "https://app.yourdomain.com"
));
```

Then redeploy:
```bash
git push origin go-back-v1
```

---

## Phase 9: Setup Monitoring

### Step 9.1: Enable Prometheus Metrics

**Already configured:**
- Application exposes `/actuator/prometheus`
- Metrics include: JVM, HTTP, cache, database

**Access metrics:**
```
https://healing-backend-prod.up.railway.app/actuator/prometheus
```

### Step 9.2: View Railway Dashboard Metrics

```
Railway Dashboard → Backend Service → Metrics tab
Shows:
  - CPU usage
  - Memory usage
  - Network I/O
  - Deployment count
  - Request/error rates
```

### Step 9.3: Optional - Deploy Grafana for Dashboards

**Advanced option (if needed):**
1. Railway Dashboard → + New → Add Grafana
2. Configure Prometheus data source: `http://prometheus:9090`
3. Create dashboards for:
   - JVM metrics (GC, heap, threads)
   - HTTP metrics (latency, errors)
   - Cache metrics (hits, misses)

---

## Phase 10: Database Backups & Maintenance

### Step 10.1: Enable Automatic Backups
1. Railway Dashboard → MySQL Service → **Data** tab
2. Under Backups section:
   - Automatic Backups: **Enabled**
   - Retention Period: **30 days** (minimum)

### Step 10.2: Restore from Backup
```
MySQL Service → Data tab → Backups
Select restore point → Click **Restore**
(This creates a new database; original kept intact)
```

### Step 10.3: Export Database (For External Backup)
```bash
# Using MySQL CLI
mysqldump -h <RAILWAY_HOST> -u <USER> -p<PASSWORD> healing_db > backup.sql

# Restore later
mysql -h <HOST> -u <USER> -p<PASSWORD> healing_db < backup.sql
```

---

## Phase 11: Scaling Configuration (Production)

### Step 11.1: Horizontal Scaling (Multiple Replicas)

```
Railway Dashboard → Backend Service → Deploy tab

Deployment Settings:
  Min Replicas: 1
  Max Replicas: 3
  
Scale Rules:
  Rule 1: If CPU > 70% for 2min → add 1 replica
  Rule 2: If CPU < 30% for 5min → remove 1 replica
```

Result: Automatically adds/removes containers based on load

### Step 11.2: Vertical Scaling (Larger Containers)

```
Railway Dashboard → Backend Service → Settings → Resource Allocation

Memory: 512MB → 1GB → 2GB (choose based on load)
CPU: 0.5 → 1 → 2 (choose based on load)

Example scaling progression:
  Dev:  0.5 CPU, 512MB RAM
  Prod: 1 CPU, 1GB RAM
  High: 2 CPU, 2GB RAM
```

### Step 11.3: Monitor & Adjust

```
Watch metrics:
  1. Check CPU/Memory usage over 7 days
  2. If consistently > 80%, scale up
  3. If consistently < 20%, scale down
  4. Monitor latency - scale if p95 > acceptable
```

---

## Phase 12: Troubleshooting

### Issue: Build Failed

```bash
# Check build logs
Railway Dashboard → Backend Service → Deployments → Build Logs

Common causes:
  ❌ Maven dependency timeout
    → Retry (internet connectivity in Railway)
  
  ❌ Java version mismatch
    → Check Dockerfile uses Java 17: FROM eclipse-temurin:17-jre
  
  ❌ Missing pom.xml
    → Ensure pom.xml exists in repo root
```

### Issue: Application Won't Start (Health Check Fails)

```bash
# Check container logs
Railway Dashboard → Backend Service → Logs

Common causes:
  ❌ Database connection failed
    → Verify DB_HOST, DB_PORT in environment
    → Check MySQL service is running
    → Verify DB_NAME=healing_db exists
  
  ❌ Redis connection failed
    → Check SPRING_REDIS_HOST environment
    → Verify Redis service running
  
  ❌ JWT_SECRET missing
    → Verify JWT_SECRET environment variable set
```

### Issue: Database Queries Slow

```bash
# Enable query logging
MySQL Service → Variables

Add:
  MYSQL_INITDB_ROOT_PASSWORD: <SET>
  MYSQL_LOG_ALL_QUERIES: yes (if available)

Then check logs for slow queries and add indexes:
  CREATE INDEX idx_doctor_location ON doctor(location_latitude, location_longitude);
```

### Issue: High Memory Usage

```bash
# Check memory metrics
Railway Dashboard → Metrics

If consistently > 80% of limit:
  1. Increase memory: Backend Service → Settings → Resource
  2. Or optimize caching: Review RedisConfig.java
  3. Or enable GC logging: Check JAVA_TOOL_OPTIONS
```

---

## Quick Reference: Railway Dashboard Navigation

```
Railway.app Home
  ↓
Your Organization / Team
  ↓
Your Project (healing_backend)
  ↓
Services:
  ├─ Backend Service
  │   ├─ Deployments (build history, logs)
  │   ├─ Domain (public URL, custom domain)
  │   ├─ Variables (environment variables)
  │   ├─ Settings (resources, scaling)
  │   ├─ Logs (real-time container output)
  │   └─ Metrics (CPU, memory, network)
  ├─ MySQL Service
  │   ├─ Variables (credentials)
  │   ├─ Data (backups, restore)
  │   └─ Logs
  └─ Redis Service
      ├─ Variables (password)
      └─ Logs
```

---

## Final Verification Checklist

- [ ] GitHub repository pushed to go-back-v1 branch
- [ ] Railway project created and connected to GitHub
- [ ] MySQL service created and database initialized (healing_db)
- [ ] Redis service created and running
- [ ] Backend environment variables set (15+ variables)
- [ ] Deployment completed successfully (check logs)
- [ ] Health endpoint returns `{"status":"UP"}`
- [ ] Public URL accessible with HTTPS
- [ ] API endpoint works: `/api/doctors?lat=...&lon=...`
- [ ] Metrics endpoint accessible: `/actuator/prometheus`
- [ ] MySQL backups enabled (30 day retention)
- [ ] Custom domain configured (optional)
- [ ] Scaling rules configured (optional)

---

**Deployment Complete! 🎉**

Your healing_backend is now running on Railway.app with:
- ✅ MySQL for persistent data
- ✅ Redis for distributed caching
- ✅ Automatic HTTPS
- ✅ Public URL
- ✅ Auto-scaling capability
- ✅ Real-time monitoring
- ✅ Backup and restore
- ✅ All performance optimizations active

**Next steps:**
1. Update mobile app backend URL to your Railway domain
2. Run load tests to verify performance
3. Monitor metrics and adjust resources if needed
4. Set up alerting for production incidents
