#!/usr/bin/env bash
# usage: scripts/reject.sh A-0001 your.name "reason"
curl -s -X POST localhost:8080/reviews/"$1"/reject -H 'content-type: application/json' -d "{\"reviewer\": \"$2\", \"reason\": \"$3\"}"; echo
