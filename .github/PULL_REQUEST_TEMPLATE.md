# Upgrade to Java 25 with Tests and Render Deployment

## Summary
This PR upgrades the project to **Java 25** and prepares it for production deployment on **Render**.

### What's Changed
- ✅ Java 25 upgrade (pom.xml, Dockerfile, compiler config)
- ✅ Fixed annotation processor conflicts (Lombok 1.18.40)
- ✅ Added comprehensive test suite (4 test classes, 22 tests, all passing)
- ✅ Added `run-tests.sh` for one-click test execution
- ✅ Configured Render deployment (render.yaml, zero-config)
- ✅ Updated Dockerfile to Temurin 25 + `.dockerignore`
- ✅ Enabled HTTP/2 & optimized compression (4KB threshold)
- ✅ Added monitoring guides (Prometheus, Grafana)

### Tests Added
| Test | Purpose |
|------|---------|
| `AllSpringBeansTest` | Verifies Spring beans exist (controllers/services) |
| `ControllersMappingTest` | Checks all @Controller/@RestController have HTTP mappings |
| `AllEndpointsSmokeTest` | MockMvc tests for all registered HTTP endpoints |
| `AllTestsSuite` | Single JUnit 5 suite to run all tests at once |

### Local Verification
```bash
# Run all tests
./run-tests.sh
# Output: BUILD SUCCESS, 22 tests passed

# Build Docker image
docker build -t healing_backend .
```

### Files Modified
- `pom.xml` — Java 25, test additions
- `Dockerfile` — Temurin 25, multi-stage build
- `.dockerignore` — optimize Docker build cache
- `src/main/resources/application-prod.properties` — HTTP/2, compression tuning
- `src/test/java/com/rickbot/healing_backend/*Test.java` — 4 new test classes
- `run-tests.sh` — one-click test script

### Files Added
- `render.yaml` — zero-config Render Blueprint
- `RENDER_DEPLOYMENT.md` — Render deploy guide
- `MONITORING_SETUP.md` — Prometheus/Grafana setup
- `.github/workflows/render-deploy.yml` — auto-deploy on push to main

### Ready for
- ✅ Merge to `main`
- ✅ Render deployment (see RENDER_DEPLOYMENT.md)
- ✅ Production monitoring (see MONITORING_SETUP.md)

### Test Results
```
Tests run: 22, Failures: 0, Errors: 0
BUILD SUCCESS
```

---

**How to Deploy to Render** (post-merge):
1. Go to https://dashboard.render.com
2. Click **New +** → **Blueprint** → select this repo
3. Render auto-reads `render.yaml`
4. Set secrets (JWT_SECRET, passwords) → Deploy

App deployed in ~5–10 min with MySQL, Redis, and health monitoring enabled.
