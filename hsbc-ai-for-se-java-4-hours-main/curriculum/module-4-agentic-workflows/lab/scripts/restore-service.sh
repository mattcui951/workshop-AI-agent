#!/usr/bin/env bash
# RESTORE SERVICE: undoes the agent's patch, so sample-service is broken again and you can re-run the agent.
cd "$(dirname "$0")/.." || exit 1
F=sample-service/src/main/java/com/atlas/recon/BusinessDays.java
if [ -f "$F.bak" ]; then mv "$F.bak" "$F" && echo "Restored the original BusinessDays.java. The service is broken again (on purpose)."
else git checkout -- sample-service 2>/dev/null; echo "No .bak file found. sample-service is in its original state."; fi
