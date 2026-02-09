#!/usr/bin/env bash
set -euo pipefail

# One-click: run the full Maven test suite
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"
./mvnw -B test "$@"
