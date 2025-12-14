# Production Deployment Checklist

## Pre-Deployment Verification

### Code & Build
- [ ] All tests passing (`./mvnw clean test`)
- [ ] Build succeeds without errors (`./mvnw -DskipTests clean package`)
- [ ] No hardcoded secrets in code or properties (use env vars or secrets manager)
- [ ] Dockerfile builds successfully
- [ ] Image tags follow semantic versioning (e.g., `v1.2.3`)
- [ ] Git commit and tag created for release

### Configuration
- [ ] `.env.example` reviewed and secrets manager integration planned
- [ ] `application-prod.properties` reviewed for all overrides
- [ ] Database connection string validated (SSL, host, port, credentials via env vars)
- [ ] Redis connection (if used) validated (SSL, host, port, password via env vars)
- [ ] JWT secret generated and stored securely
- [ ] Logging levels appropriate for production (INFO/WARN, no DEBUG)

### Database
- [ ] Database schema migrated (Flyway/Liquibase scripts run or manual migration completed)
- [ ] Indexes created for common queries (especially `doctor.location_latitude/longitude` for radius searches)
- [ ] Backups enabled and tested
- [ ] Connection pooling tuned (HikariCP settings match environment)

### Infrastructure
- [ ] Container resource limits set (memory, CPU)
- [ ] JVM flags configured and tuned (G1GC, MaxRAMPercentage, heap dump on OOM)
- [ ] Networking: firewall rules, DNS, load balancer configured
- [ ] Reverse proxy (Nginx/HAProxy) configured with gzip, SSL/TLS termination, and header rules

### Observability
- [ ] Prometheus scraping configured and working
- [ ] Grafana dashboards set up (JVM, latency, error rate, cache hit/miss)
- [ ] Alerting rules created and tested (high error rate, high p95 latency, low cache hit, high GC time)
- [ ] Centralized logging (ELK/EFK) configured if available
- [ ] Distributed tracing (OpenTelemetry/Jaeger) optional but recommended

### Security
- [ ] HTTPS/TLS enabled (cert provisioning, auto-renewal, HSTS headers)
- [ ] API authentication (JWT) validated
- [ ] SQL injection risk minimized (parameterized queries used; native queries reviewed)
- [ ] CORS policies reviewed and locked down
- [ ] Rate limiting / WAF in place to prevent abuse
- [ ] Secrets rotation schedule established

### Performance & Load Testing
- [ ] Staging environment performance tested (k6, wrk, or Gatling)
- [ ] JFR profile collected under representative load
- [ ] Latency (p95, p99), error rates, and resource utilization validated
- [ ] Cache hit rates monitored
- [ ] Database slow-query log reviewed and optimized

### Deployment & Rollback
- [ ] Blue/green or canary deployment plan in place
- [ ] Rollback procedure documented and tested
- [ ] Health check endpoint verified (`/actuator/health`)
- [ ] Readiness/liveness probes configured (for K8s or similar)

### Documentation
- [ ] Runbook created for common operations (restart, scale, update)
- [ ] Emergency contacts and escalation procedures documented
- [ ] Known issues and workarounds documented

## Deployment Steps

1. **Build & push image**
   ```bash
   docker build -t your-registry/healing-backend:v1.0.0 .
   docker push your-registry/healing-backend:v1.0.0
   ```

2. **Set environment variables / secrets**
   ```bash
   # Via .env file or secrets manager
   export SPRING_PROFILES_ACTIVE=prod
   export DB_HOST=...
   export REDIS_HOST=...
   # etc.
   ```

3. **Run database migrations** (if applicable)
   ```bash
   # Flyway or Liquibase will auto-run on startup, or manual:
   # mysql -h $DB_HOST -u $DB_USER -p < migrations/001_init.sql
   ```

4. **Start containers** (Docker Compose or orchestrator)
   ```bash
   docker run -e SPRING_PROFILES_ACTIVE=prod \
     -e DB_HOST=$DB_HOST \
     -e REDIS_HOST=$REDIS_HOST \
     -p 8080:8080 \
     your-registry/healing-backend:v1.0.0
   ```
   OR (Kubernetes)
   ```bash
   kubectl apply -f k8s/deployment.yaml
   kubectl rollout status deployment/healing-backend
   ```

5. **Health check & smoke tests**
   ```bash
   curl http://your-app:8080/actuator/health
   # Run critical API smoke tests
   ```

6. **Monitor metrics & logs**
   - Check Prometheus for metric scraping
   - Verify Grafana dashboards update
   - Check centralized logs for errors
   - Watch error rates for 10+ minutes post-deployment

7. **Announce and monitor**
   - Notify team of deployment
   - Set up on-call alerts
   - Continue monitoring for 1-2 hours

## Post-Deployment
- [ ] Verify all metrics in Prometheus/Grafana
- [ ] Confirm database connectivity and replication status
- [ ] Cache hit rates acceptable (>50% for read-heavy workloads)
- [ ] No spike in error rates
- [ ] Latency metrics stable
- [ ] Document any findings and feedback for next release

## Rollback Steps
If issues detected:
1. Revert to previous image version
2. Restart containers
3. Verify health checks pass
4. Monitor metrics to confirm recovery
5. Investigate root cause and document

---

## Production Environment Variables (Example)

Set these via your deployment platform (Docker secrets, K8s secrets, environment manager):

```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=mysql.prod.example.com
DB_PORT=3306
DB_NAME=healing_prod
DB_USER=app_user
DB_PASSWORD=<SECURE_PASSWORD>
REDIS_HOST=redis.prod.example.com
REDIS_PORT=6379
REDIS_PASSWORD=<SECURE_PASSWORD>
REDIS_SSL=true
JWT_SECRET=<LONG_RANDOM_STRING_MIN_64_CHARS>
JAVA_TOOL_OPTIONS=-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError
```

---

## Additional Resources
- See `PRODUCTION_FRONTEND_RUNBOOK.md` for frontend and ops details
- See `Dockerfile` for container configuration
- See `docker/docker-compose.yml` for local testing setup
- See `docker/prometheus.yml` for metrics scraping config
