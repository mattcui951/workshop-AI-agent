#!/usr/bin/env bash
# usage: scripts/ask.sh "your question"      (no copy/paste needed: short commands only)
curl -s -X POST localhost:8080/ask -H 'content-type: application/json' \
  -d "{\"question\": \"$*\"}"; echo
