#!/usr/bin/env bash
# TEST GATE: checks your fix to makeGate in PrReviewApp.java (4 tests). At the start, 3 fail.
cd "$(dirname "$0")/.." || exit 1
mvn -q test -Dtest=PrReviewGateTest
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed. Read the lines that start with [ERROR] above: they say which test and why."; fi
