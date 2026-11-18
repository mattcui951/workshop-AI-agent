#!/usr/bin/env bash
# INSTRUCTOR ONLY: replays a scripted conversation when there is no API key or no network. It is NOT a model.
#   bash scripts/offline-server.sh agent        (then, in another terminal, see the speaker notes)
#   bash scripts/offline-server.sh pr-review
cd "$(dirname "$0")/.." || exit 1
mvn -q test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.ga.hsbc.module4.OfflineRehearsalServer -Dexec.args="${1:-agent} 8089"
