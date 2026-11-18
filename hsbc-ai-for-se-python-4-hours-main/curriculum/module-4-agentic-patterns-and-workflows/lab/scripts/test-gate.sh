#!/usr/bin/env bash
# TEST GATE: checks your fix to make_gate in pr_review_app.py (4 tests). At the start, 3 fail.
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
python3 -m pytest -q -p no:cacheprovider tests/test_pr_review_gate.py
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed or errored. Read the lines that start with FAILED or ERROR above: they say which test and why."; fi
