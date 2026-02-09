# Monitoring Setup Guide

Your Healing Backend is instrumented with **Prometheus metrics** and **Spring Boot Actuator** for production-grade observability.

## Metrics Available

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Health check (liveness probe) |
| `/actuator/prometheus` | Prometheus scrape endpoint (metrics) |
| `/actuator/metrics` | Detailed metrics in JSON |
| `/actuator/info` | Application info & version |

## Quick Start with Docker Compose (Local)

### 1. Run Stack Locally
```bash
docker-compose -f docker/docker-compose.yml up -d
```

This starts:
- **Healing Backend** on `http://localhost:8080`
- **Prometheus** on `http://localhost:9090`
- **Grafana** on `http://localhost:3000` (admin / admin)

### 2. Verify Metrics Collection

**Prometheus scraping:**
```bash
curl http://localhost:9090/api/v1/targets
```

**Healing Backend metrics:**
```bash
curl http://localhost:8080/actuator/prometheus | head -20
```

### 3. Add Grafana Dashboard

1. Login to Grafana (http://localhost:3000)
   - User: `admin` / Password: `admin`
2. Add Prometheus datasource:
   - URL: `http://prometheus:9090`
3. Import pre-built Spring Boot dashboard:
   - ID: `4701` or `14891` (Grafana marketplace)
4. Or create custom dashboard:

#### Useful Queries

**JVM Heap Usage:**
```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

**HTTP Request Rate (5m average):**
```promql
rate(http_server_requests_seconds_count[5m])
```

**HTTP Error Rate:**
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

**Cache Hit Ratio:**
```promql
cache_gets_total{result="hit"} / (cache_gets_total{result="hit"} + cache_gets_total{result="miss"})
```

**DB Connection Pool:**
```promql
hikaricp_connections_active
```

**GC Time:**
```promql
increase(jvm_gc_pause_seconds_sum[5m])
```

---

## Production Monitoring on Render

### 1. Expose Metrics from Render App

Once deployed on Render, your app URL is: `https://healing-backend-xxxxx.onrender.com`

Health check:
```bash
curl https://healing-backend-xxxxx.onrender.com/actuator/health
```

Prometheus metrics:
```bash
curl https://healing-backend-xxxxx.onrender.com/actuator/prometheus
```

### 2. Add to External Prometheus

If you run Prometheus externally (self-hosted or cloud), add this scrape config:

**prometheus.yml:**
```yaml
global:
  scrape_interval: 15s
  
scrape_configs:
  - job_name: 'healing-backend-render'
    static_configs:
      - targets: ['healing-backend-xxxxx.onrender.com:443']
    scheme: https
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
    scrape_timeout: 10s
    # Optional: authentication if needed
    # basic_auth:
    #   username: 'user'
    #   password: 'pass'
```

### 3. View Logs in Render Dashboard

- Log in to https://dashboard.render.com
- Select service → **Logs** tab
- Tail in real-time or search by keyword

### 4. Alerting (Optional)

Set up alerts in Prometheus/Grafana for:

**High Error Rate (>5% of requests are 5xx):**
```yaml
- alert: HighErrorRate
  expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.05
  for: 5m
```

**Low Memory (>80% heap used):**
```yaml
- alert: LowMemory
  expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.8
  for: 5m
```

**High GC Pause Time (>1s in 5m window):**
```yaml
- alert: HighGCPause
  expr: rate(jvm_gc_pause_seconds_sum[5m]) > 1
  for: 5m
```

---

## Key Metrics to Watch

| Metric | Threshold | Action |
|--------|-----------|--------|
| HTTP 5xx rate | >5% | Check app logs, investigate errors |
| JVM heap usage | >80% | Increase container memory or tune cache |
| DB connection pool active | >15 of 20 | May indicate DB bottleneck, check queries |
| Cache hit ratio | <70% | Review cache TTL/size configs |
| GC pause time | >500ms | Tune G1GC settings or reduce allocation |
| Response time p99 | >2s | Profile with JFR, check slow queries |

---

## Profiling (Advanced)

For deep-dive performance analysis, use JFR (Java Flight Recorder):

```bash
# Start app with JFR recording
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=profile.jfr,duration=60s" \
  ./mvnw spring-boot:run

# Analyze with JDK Mission Control or dump to stdout
jcmd <pid> JFR.dump filename=profile.jfr
```

See `scripts/README_PROFILING.md` for more details.

---

## Troubleshooting

**No metrics appearing in Prometheus?**
- Check target status: http://prometheus:9090/targets
- Verify scrape endpoint is reachable: `curl http://app:8080/actuator/prometheus`
- Check Prometheus logs for scrape errors

**High memory usage?**
- Check `jvm_gc_pause_seconds_sum` for GC pressure
- Review cache sizes in `application-prod.properties`
- Profile with JFR (see Profiling section)

**Database connection pool exhausted?**
- Check active connections: `hikaricp_connections_active`
- Increase pool size in `application-prod.properties` (default 20 max)
- Investigate slow queries in MySQL logs

---

## Next Steps

1. **Deploy to Render** → run `render.yaml`
2. **Add external Prometheus** → scrape metrics from Render URL
3. **Set up Grafana dashboards** → visualize metrics
4. **Configure alerts** → get notified of issues
5. **Profile regularly** → optimize hot spots

---

See also:
- [Spring Boot Actuator Docs](https://spring.io/guides/gs/actuator-service/)
- [Prometheus Docs](https://prometheus.io/docs/)
- [Grafana Docs](https://grafana.com/docs/)
