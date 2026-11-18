#!/usr/bin/env bash
# TEST SERVICE: runs sample-service's own tests (5). Broken service: 3 fail. Fixed service: all 5 pass.
cd "$(dirname "$0")/../sample-service" || exit 1
mvn -q test
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed. Read the lines that start with [ERROR] above: they say which test and why."; fi
