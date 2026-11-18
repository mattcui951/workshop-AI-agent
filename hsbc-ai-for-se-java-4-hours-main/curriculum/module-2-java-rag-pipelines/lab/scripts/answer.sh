#!/usr/bin/env bash
# usage: scripts/answer.sh A-0001           what the REQUESTER can see
curl -s -i localhost:8080/answers/"$1" | sed -n '1p;$p'
