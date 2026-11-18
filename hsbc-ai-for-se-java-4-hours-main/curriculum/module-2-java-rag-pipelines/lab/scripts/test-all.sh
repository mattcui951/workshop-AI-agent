#!/usr/bin/env bash
# TEST ALL: runs every test in this lab (32 tests).
cd "$(dirname "$0")/.." || exit 1
mvn -q test
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed. Read the lines that start with [ERROR] above: they say which test and why."; fi
