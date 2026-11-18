#!/usr/bin/env bash
# usage: scripts/approve.sh A-0001 your.name
curl -s -X POST localhost:8080/reviews/"$1"/approve -H 'content-type: application/json' -d "{\"reviewer\": \"$2\"}"; echo
