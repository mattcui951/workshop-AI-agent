#!/usr/bin/env bash
# TEST ALL: every test in the agent project (55).
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
if [ -f sample_repo/portfolio.py.bak ]; then
  echo "sample_repo is currently PATCHED. Some agent tests need the original (broken) code."
  echo "Run: bash scripts/restore-service.sh   then run this again."
  exit 1
fi
python3 -m pytest -q -p no:cacheprovider
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed or errored. Read the lines that start with FAILED or ERROR above: they say which test and why."; fi
