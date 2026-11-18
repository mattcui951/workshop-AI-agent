#!/usr/bin/env bash
# SERVICE STATUS: tells you whether the agent changed sample_repo.
cd "$(dirname "$0")/.." || exit 1
if [ -f sample_repo/portfolio.py.bak ]; then echo "CHANGED: a patch was applied. The original is saved as portfolio.py.bak"
else echo "UNCHANGED: no patch has been applied to sample_repo."; fi
