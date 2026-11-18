#!/usr/bin/env bash
# TEST ALL: every test in the agent project (48).
cd "$(dirname "$0")/.." || exit 1
if [ -f sample-service/src/main/java/com/atlas/recon/BusinessDays.java.bak ]; then
  echo "sample-service is currently PATCHED. Some agent tests need the original (broken) service."
  echo "Run: bash scripts/restore-service.sh   then run this again."
  exit 1
fi
mvn -q test
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed. Read the lines that start with [ERROR] above: they say which test and why."; fi
