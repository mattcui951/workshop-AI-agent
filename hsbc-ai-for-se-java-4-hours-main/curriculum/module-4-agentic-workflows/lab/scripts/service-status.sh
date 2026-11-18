#!/usr/bin/env bash
# SERVICE STATUS: tells you whether the agent changed sample-service.
cd "$(dirname "$0")/.." || exit 1
F=sample-service/src/main/java/com/atlas/recon/BusinessDays.java
if [ -f "$F.bak" ]; then echo "CHANGED: a patch was applied. The original is saved as BusinessDays.java.bak"
else echo "UNCHANGED: no patch has been applied to sample-service."; fi
git status --short -- sample-service 2>/dev/null
