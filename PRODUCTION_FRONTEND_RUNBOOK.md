# Frontend & Production Optimization Runbook

This runbook gathers everything you should do on the frontend and in production to optimize performance, memory/CPU usage and response times for the `healing_backend` service. Follow these steps iteratively: measure → prioritize → change → measure again.

**How to use**
- Start by profiling (JFR + async-profiler) and load-testing (k6/wrk) in a staging environment that mirrors production.
- Apply the lowest-risk, highest-impact changes first (caching, DB indexes, remove N+1, logging changes).
- Keep observability in place (Prometheus/Grafana, metrics, tracing) to verify improvements.

**Files created/edited in repo**
- `src/main/resources/application.properties` — actuator, cache defaults (already added)
- `src/main/java/.../config/CacheConfig.java` — Caffeine cache (already added)
- `src/main/java/.../config/MetricsConfig.java` — Micrometer tags (already added)
- `Dockerfile` — runtime flags (already added)
- `scripts/jfr-record.sh` — JFR helper (already added)

---

## Frontend (Client) Optimizations

- **Use CDN for static assets**: host JS/CSS/images/fonts on CDN (Cloudflare, Fastly, S3+CloudFront). Set long cache TTLs for immutable assets (fingerprint filenames).
- **Enable HTTP/2 or HTTP/3** on CDN and web server to reduce round-trips.
- **Compress responses** with Brotli (preferred) or gzip on the CDN or reverse proxy.
- **Minify and bundle assets**: use tree-shaking and code-splitting. Serve smaller JS bundles and lazy-load non-critical routes.
- **Critical CSS & inlining**: inline above-the-fold CSS; defer other styles.
- **Optimize images**: use modern formats (AVIF/WebP), responsive sizes, lazy loading, and a CDN with image optimization.
- **Fonts**: host locally or via CDN, use `font-display: swap`, preload key fonts, subset fonts.
- **Remove or defer third-party scripts**: tag manager, analytics; load async or via worker.
- **Use Service Worker / PWA** for caching and offline (carefully manage cache invalidation).
- **Use HTTP caching**: server should set `Cache-Control`, `ETag`, `Last-Modified` appropriately.
  - Example headers for immutable assets: `Cache-Control: public, max-age=31536000, immutable`.
  - For API responses, set `Cache-Control: private, max-age=60, must-revalidate` or use conditional GETs.
- **Optimize API usage**: request only needed fields; use smaller DTOs; use pagination and infinite scrolling; debounce user actions.
- **Client-side caching & storage**: cache API responses in-memory or IndexedDB where appropriate; use stale-while-revalidate pattern.
- **Use performance budgets and monitoring**: Lighthouse CI, Web Vitals, Sentry, or real-user monitoring (RUM).
- **Measure**: Use Lighthouse, WebPageTest, or RUM (Core Web Vitals) to track improvements.

Commands / examples (frontend)

- Build and fingerprint static assets (example with npm):

```bash
# build for production
npm run build
# deploy build/ to CDN or static host
```

- Example HTTP header for API responses (Nginx):

```nginx
location /api/ {
  proxy_pass http://backend:8080;
  add_header Cache-Control "private, max-age=60, must-revalidate";
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

---

## Backend / Production Optimizations

### Observability & Profiling (first step)
- **Enable Actuator & Prometheus**: `/actuator/prometheus` endpoint exists (already added). Scrape with Prometheus.
- **Add tracing** (OpenTelemetry/Jaeger) to follow request traces and measure latency across services.
- **Profile under load**:
  - Use JFR to collect CPU/Garbage/Lock/Allocation events.
  - Use `async-profiler` for CPU and allocation flamegraphs.
- **Load testing**: create representative scenarios with k6, wrk, or Gatling and run against staging.

Run commands (JFR + async-profiler)

```bash
# JFR (jcmd) - record 120s
jcmd <PID> JFR.start name=profile,settings=profile,duration=120s,filename=/tmp/profile.jfr
# After recording, inspect with Java Mission Control or jfr tool
jfr summary /tmp/profile.jfr

# async-profiler (on Linux/staging):
./profiler.sh -e cpu -d 60 -f cpu.svg <PID>
./profiler.sh -e alloc -d 60 -f alloc.svg <PID>
```

### JVM & Container
- **JVM flags**: choose GC based on latency requirements and heap size.
  - `G1GC` is a good general option: `-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:MaxRAMPercentage=75.0`
  - For very large heaps or strict pause targets, consider `ZGC` or `Shenandoah` when available.
- **Use `MaxRAMPercentage`** to let JVM adapt inside containers: `-XX:MaxRAMPercentage=75.0`.
- **Enable heap dumps on OOME**: `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap.hprof`.
- **Pass resource limits from container orchestrator** (Kubernetes): set `resources.limits.memory/cpu` and `requests`.

Docker example (already updated in `Dockerfile`):

```Dockerfile
ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError"
ENTRYPOINT ["sh","-c","java $JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError -jar app.jar"]
```

Kubernetes deployment snippet (resource requests/limits + readiness/liveness):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: healing-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: healing-backend
  template:
    metadata:
      labels:
        app: healing-backend
    spec:
      containers:
      - name: app
        image: your-registry/healing-backend:latest
        resources:
          requests:
            cpu: "500m"
            memory: "1Gi"
          limits:
            cpu: "2000m"
            memory: "3Gi"
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 30
```

- **Horizontal Pod Autoscaler**: base scaling on CPU or custom metrics (request latency, queue length, Prometheus metrics).

### Database
- **Enable slow query logging** and collect slow queries. For MySQL, set `slow_query_log` and `long_query_time`.
- **Index high-cardinality WHERE / ORDER BY / JOIN columns**; avoid `SELECT *`.
- **Use pagination and projections** instead of loading full entities when you only need a few columns.
- **Use connection pooling** (HikariCP) and tune pool size to match DB capacity and concurrency. Already configured in `application.properties` (tune `maximum-pool-size` accordingly).
- **Use read replicas** for read-heavy workloads; route heavy read endpoints to replicas.
- **For spatial radius searches** (finding doctors by distance) use DB-side spatial index (PostGIS for Postgres or spatial extensions for MySQL) instead of fetching all rows.

Example `EXPLAIN ANALYZE` workflow

```sql
EXPLAIN ANALYZE SELECT id, name, latitude, longitude FROM doctors WHERE ST_Distance_Sphere(location, POINT(lon, lat)) < 50000;
```

### Caching
- **Use Redis for distributed caching** if you have multiple instances or need shared cache.
  - Use `RedisCacheManager` with appropriate TTLs and eviction policies.
- **Cache patterns**:
  - Cache query results for read-heavy endpoints (`@Cacheable`).
  - Cache computed values (distance calculations if input coordinates repeat) with a composite key.
  - Use `Cache-Aside` or `Write-Through` strategies depending on consistency needs.
- **Invalidate caches on writes**: use `@CacheEvict` or publish invalidation events.

Redis example (pseudo Spring config)

```yaml
spring:
  redis:
    host: redis
    port: 6379
```

### Threading & Async
- **Use separate thread pools** for blocking IO (DB, network) and CPU-bound tasks.
- **Limit max threads** to avoid context switching overhead. Tune pool sizes based on CPU count and blocking coefficient.
- **Use asynchronous processing** for long-running tasks and background jobs (e.g., `@Async`, messages). Use message queues (RabbitMQ, Kafka) for decent throughput and decoupling.

### Logging
- **Use async logging** (already done) to avoid blocking.
- **Reduce log level** in production (INFO) and use DEBUG only for troubleshooting; avoid logging large payloads in hot paths.
- **Centralize logs** to ELK/EFK for searching and alerting.

### Resiliency
- **Circuit breakers and rate limiting**: use Resilience4j to prevent cascading failures.
- **Retry with exponential backoff** for transient errors.
- **Rate-limit external endpoints** and protect against abusive traffic (API gateway/WAF).

### Security & Secrets
- Use a secrets manager (Vault, AWS Secrets Manager) for DB credentials, JWT secrets.
- Rotate secrets regularly and avoid hard-coding credentials in `application.properties`.
- Terminate TLS at load balancer or ingress; enforce HTTPS.

### CI/CD & Deployments
- **Build reproducible images**: use multi-stage Docker builds (already used). Keep image size small.
- **Use canary or blue/green deployments** for production changes.
- **Automate migrations** (Flyway/Liquibase) and include a rollback plan.
- **Run integration & smoke tests** in staging before promoting to production.

### Backups & DR
- **Automate DB backups** and test restore procedures.
- **Snapshot critical storage** and ensure RTO/RPO meet SLAs.

---

## Monitoring, Alerts & Dashboards
- **Prometheus** scraping `/actuator/prometheus`.
- **Grafana** dashboards for: request latency (p50/p95/p99), error rates, CPU usage, heap/GC time, thread counts, DB pool usage, cache hit/miss rates.
- **Alerts**: high error rate, high p95 latency, GC pause > X ms, heap nearing X% of max, low cache hit rate.

Sample Prometheus scrape config

```yaml
scrape_configs:
  - job_name: 'healing-backend'
    static_configs:
      - targets: ['your-hostname:8080']
    metrics_path: /actuator/prometheus
```

Suggested Grafana panels
- Request rate and latency (histogram/percentiles)
- JVM heap & GC time
- CPU usage
- HikariCP active / total connections
- Cache hit ratio (Caffeine / Redis) and eviction rate
- DB slow queries / top 10 slow endpoints

---

## Runbook: Quick Commands & Examples

- Build and run locally (skip tests):

```bash
./mvnw -DskipTests clean package
java -jar -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -jar target/*.jar
```

- Docker run with limits:

```bash
docker run --rm -p 8080:8080 --memory=3g --cpus=2 your-image:latest
```

- Start JFR via helper script (repo `scripts/jfr-record.sh`):

```bash
# get pid
jcmd -l
# run
./scripts/jfr-record.sh <PID> 120 /tmp/profile.jfr
```

- async-profiler (Linux/staging):

```bash
./profiler.sh -e cpu -d 60 -f cpu.svg <PID>
```

- k6 quick load test example (install https://k6.io):

```js
// scripts/load-test.js
import http from 'k6/http';
import { sleep } from 'k6';
export default function () {
  http.get('https://staging.yoursite/api/doctors?lat=...&lon=...');
  sleep(1);
}
```

Run it:

```bash
k6 run --vus 50 --duration 60s scripts/load-test.js
```

- MySQL slow query: enable and inspect `slow_query_log` and run `EXPLAIN`.

---

## Suggested Next Actions (priority)
1. Profile current staging under representative load (JFR + async-profiler). Collect CPU and allocation hotspots.
2. Enable slow-query logging and collect slow queries for the DB.
3. Implement Redis only if you need shared cache across instances; else keep Caffeine.
4. Add Grafana dashboards and alerts for key SLOs.
5. Replace Java-side radius search (fetch-all) with DB-side spatial queries using spatial indexes.
6. Use canary deployments for any DB or schema changes.

---

If you want, I can:
- Add a `docker-compose.yml` for local testing with Redis + Postgres/MySQL + Prometheus + Grafana.
- Create a sample `k6` load-test script tailored for your API endpoints.
- Implement `RedisCacheManager` + feature flag to toggle Redis vs Caffeine.
- Generate Terraform / Kubernetes Deployment and HPA manifests for production deployment.

Tell me which of the above I should create next and I'll add them directly into the repository.
