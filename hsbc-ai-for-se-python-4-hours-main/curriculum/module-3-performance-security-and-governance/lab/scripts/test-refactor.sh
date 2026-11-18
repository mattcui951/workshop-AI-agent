#!/usr/bin/env bash
# TEST REFACTOR: runs the safety net for reconciler.py. At the start, 1 test fails on purpose (the performance test).
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
python3 -m pytest -q -p no:cacheprovider tests/test_reconciler.py tests/test_oracle.py tests/test_performance.py
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed or errored. Read the lines that start with FAILED or ERROR above: they say which test and why."; fi
