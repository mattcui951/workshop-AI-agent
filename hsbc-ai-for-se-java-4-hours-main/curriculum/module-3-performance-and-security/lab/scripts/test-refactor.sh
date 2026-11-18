#!/usr/bin/env bash
# TEST REFACTOR: runs the safety net for TradeReconciler (12 tests). At the start, 1 fails on purpose (PerformanceTest).
cd "$(dirname "$0")/../refactor" || exit 1
mvn -q test
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed. Read the lines that start with [ERROR] above: they say which test and why."; fi
