#!/usr/bin/env bash
# RESTORE SERVICE: undoes the agent's patch, so sample_repo is broken again and you can re-run the agent.
cd "$(dirname "$0")/.." || exit 1
F=sample_repo/portfolio.py
if [ -f "$F.bak" ]; then mv "$F.bak" "$F" && echo "Restored the original portfolio.py. The code is broken again (on purpose)."
else git checkout -- sample_repo 2>/dev/null; echo "No .bak file found. sample_repo is in its original state."; fi
