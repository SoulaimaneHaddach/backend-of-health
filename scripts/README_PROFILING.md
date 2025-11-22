Profiling helpers

- `jfr-record.sh` - wrapper to start a Java Flight Recorder (JFR) recording using `jcmd`.

Async-profiler (native) instructions:
- Download async-profiler from https://github.com/jvm-profiling-tools/async-profiler and extract.
- Run CPU profile for 60s: `./profiler.sh -d 60 -f cpu.svg <PID>`
- Run allocation profile: `./profiler.sh -e alloc -d 60 -f alloc.svg <PID>`

General steps:
1. Run a representative load test against staging (use `k6`, `wrk`, or `gatling`).
2. Start JFR while the load runs, and download the `.jfr` file for analysis (use `jfr` tool or Java Mission Control).
3. Use async-profiler to get flamegraphs for CPU and allocation hotspots.
4. Iterate on code changes and re-profile to measure improvements.
