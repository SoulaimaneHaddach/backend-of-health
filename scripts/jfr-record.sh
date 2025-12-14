#!/usr/bin/env bash
# Usage: ./jfr-record.sh <PID> <durationSeconds> <output.jfr>
PID=${1}
DURATION=${2:-120}
OUT=${3:-/tmp/profile.jfr}

if [ -z "$PID" ]; then
  echo "Usage: $0 <PID> [durationSeconds] [output.jfr]"
  exit 1
fi

echo "Starting JFR on PID=$PID for ${DURATION}s -> ${OUT}"
jcmd $PID JFR.start name=opt,settings=profile,duration=${DURATION}s,filename=${OUT}

echo "Recording..."

# The jcmd JFR.start will stop after duration, but we wait slightly longer
sleep $((DURATION + 5))

echo "Done. JFR saved to ${OUT}"
