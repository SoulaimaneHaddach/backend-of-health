# Complete Deployment & Release Runbook

This document provides **step-by-step instructions** to go from code to production.

---

## Phase 1: Code Review & Merge (GitHub)

### 1. Open Pull Request
```
https://github.com/MustaphaAbk/healing_backend/pull/new/upgrade/java-25
```

- Title: "Upgrade to Java 25 with tests and Render deployment"
- Assign reviewers
- Verify GitHub Actions CI passes (auto-runs on PR creation)

### 2. Merge to Main
Once approved:
```bash
# GitHub UI: Click "Merge pull request" → "Confirm merge"
# OR via CLI (if gh is installed):
gh pr merge --merge
```

---

## Phase 2: Deploy to Render (5-10 min)

### Prerequisites
- Render account (free tier available): https://render.com
- GitHub account connected to Render

### Step-by-Step Deploy

**1. Create Render Blueprint Service**

```
a) Go to https://dashboard.render.com/blueprints
b) Click "New Blueprint"
c) Select repo: MustaphaAbk/healing_backend
d) Select branch: main (after merge) or upgrade/java-25 (for testing)
e) Render auto-detects render.yaml
```

**2. Configure Environment & Secrets**

When prompted, provide:

| Secret | Example | Notes |
|--------|---------|-------|
| `JWT_SECRET` | `your-256-bit-hex-key` | Use `openssl rand -hex 32` to generate |
| `MYSQL_PASSWORD` | `auto-generated-or-your-choice` | Leave blank for auto-generate |
| `MYSQL_ROOT_PASSWORD` | `auto-generated-or-your-choice` | Leave blank for auto-generate |
| `SPRING_REDIS_PASSWORD` | `auto-generated-or-your-choice` | Leave blank for auto-generate |

Generate JWT_SECRET:
```bash
openssl rand -hex 32
# Output: a1b2c3d4e5f6... (copy this)
```

**3. Deploy**

Click **"Deploy Blueprint"** → Render builds and deploys:
- Downloads source code
- Builds Docker image (uses `Dockerfile`)
- Starts MySQL 8.0 + Redis 7
- Runs Spring Boot app
- Auto health checks enabled

**Estimated time:** 5–10 minutes

### Post-Deploy

**App URL:** https://healing-backend-xxxxx.onrender.com (displayed in Render dashboard)

**Verify deployment:**
```bash
curl https://healing-backend-xxxxx.onrender.com/actuator/health
# Response: {"status":"UP","...}
```

**View logs:**
- Render dashboard → Service → Logs tab
- Real-time tail or search

**Scale instances:**
- Render dashboard → Service → Settings
- Adjust Min/Max instances (currently 1–3)

---

## Phase 3: Setup Monitoring (Optional)

### Option A: Local Docker Stack (Testing)
```bash
docker-compose -f docker/docker-compose.yml up -d
# Starts: app (8080), Prometheus (9090), Grafana (3000)
```

**Verify:**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- App: http://localhost:8080

### Option B: External Monitoring (Production)

**Add Render app to Prometheus:**

Update your Prometheus config:
```yaml
scrape_configs:
  - job_name: 'healing-backend-prod'
    static_configs:
      - targets: ['healing-backend-xxxxx.onrender.com:443']
    scheme: https
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
```

**Create Grafana dashboards:**
1. Add Prometheus datasource
2. Import dashboard ID `4701` (Spring Boot 2.x/3.x)
3. Or manually create from queries (see MONITORING_SETUP.md)

---

## Phase 4: Post-Deployment Checklist

After deployment succeeds, verify:

- [ ] **Health Check**
  ```bash
  curl https://healing-backend-xxxxx.onrender.com/actuator/health
  ```
  Response: `{"status":"UP"}`

- [ ] **Database Connection**
  - Check Render logs for schema migrations
  - Verify MySQL started: `ps aux | grep mysql`

- [ ] **Redis Connection**
  - Check logs for "Connected to Redis"
  - Test cache: make API call twice, check response times

- [ ] **API Endpoints**
  ```bash
  curl https://healing-backend-xxxxx.onrender.com/api/doctors/nearby?lat=40&lon=-74
  # Should return doctor list or 401 (if auth required)
  ```

- [ ] **Metrics Collection**
  ```bash
  curl -s https://healing-backend-xxxxx.onrender.com/actuator/prometheus | head
  # Should show Prometheus metrics
  ```

- [ ] **Logs Flowing**
  - Render dashboard → Logs tab
  - Verify no errors on startup

---

## Phase 5: Configure Auto-Deploy (Optional)

Enable automatic deployment when you push to `main`:

**1. Get Render API Key**
```
https://dashboard.render.com/account/api-tokens
Create new token → copy
```

**2. Add GitHub Secrets**
```bash
# Add two secrets to GitHub repo:
# RENDER_API_KEY = <your Render API token>
# RENDER_SERVICE_ID = <service ID from Render dashboard>
```

GitHub repo → Settings → Secrets and variables → Actions → New secret

**3. Verify Workflow**
```
Push to main → GitHub Actions runs → .github/workflows/render-deploy.yml executes
→ Sends deploy request to Render → App redeploys automatically
```

View workflow: GitHub repo → Actions tab

---

## Phase 6: Monitoring & Maintenance

### Daily
- Check Render logs for errors
- Monitor `/actuator/health` endpoint
- Watch error rate via Prometheus/Grafana

### Weekly
- Review Render service metrics (CPU, memory)
- Check database growth (MySQL storage)
- Verify backup/snapshot retention

### Monthly
- Rotate secrets (JWT_SECRET, passwords)
- Update dependencies: `mvn versions:display-dependency-updates`
- Review performance metrics, optimize if needed

### Scaling
If traffic increases:
- Increase Render `maxInstances` (currently 3)
- Upgrade MySQL plan (storage/compute)
- Add Redis cluster if cache hit ratio drops

---

## Troubleshooting

### App Won't Start
**Check logs:**
```
Render dashboard → Service → Logs
Look for: "Failed to start", "Connection refused", "Out of memory"
```

**Common causes:**
- MySQL not running: wait 2–3 min for DB initialization
- JWT_SECRET not set: verify secret in Render dashboard
- Port conflict: Render enforces `SERVER_PORT=8080`

**Solution:**
1. Check logs for root cause
2. Fix in code or config
3. Push to main → auto-redeploy or click "Redeploy" in Render

### High Memory Usage
**Check:**
```bash
# Prometheus query
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

**If >80%:**
- Increase Render container memory (Settings → Plan)
- Reduce cache size: edit `application-prod.properties`
- Profile with JFR: see `scripts/README_PROFILING.md`

### Slow Queries
**Profile:**
```bash
# Start JFR profiling
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=..." \
  java -jar app.jar
```

**Analyze:**
- Check MySQL slow query log
- Review `SHOW PROCESSLIST` for blocking queries
- Add database indexes as needed

---

## Rollback Procedures

### Quick Rollback (If Auto-Deploy Failed)
```
Render dashboard → Service → Deployments
Click previous successful deployment → "Rollback"
```

### Manual Rollback (If Code Has Issues)
```bash
# Revert last commit locally
git revert HEAD
git push origin main

# Render auto-redeploys with reverted code
```

---

## Resources

| Topic | Link |
|-------|------|
| Render Docs | https://render.com/docs |
| render.yaml Reference | https://render.com/docs/infrastructure-as-code |
| Spring Boot Actuator | https://spring.io/guides/gs/actuator-service/ |
| Prometheus Queries | https://prometheus.io/docs/prometheus/latest/querying/basics/ |
| Grafana Dashboards | https://grafana.com/grafana/dashboards/ |
| MySQL on Render | https://render.com/docs/databases |

---

## Quick Reference

```bash
# Local testing before deploy
./mvnw -B -DskipTests package
docker build -t healing_backend .
docker run -p 8080:8080 healing_backend

# Check test coverage
./mvnw test

# All tests in one go
./run-tests.sh

# View logs locally
docker-compose -f docker/docker-compose.yml logs -f

# Generate JWT_SECRET
openssl rand -hex 32
```

---

**Need help?** Check logs in Render dashboard or see [MONITORING_SETUP.md](MONITORING_SETUP.md) for observability.
