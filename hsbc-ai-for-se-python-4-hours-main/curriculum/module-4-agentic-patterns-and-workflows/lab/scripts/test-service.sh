#!/usr/bin/env bash
# TEST SERVICE: runs sample_repo's own tests (5). Broken: 3 fail. Fixed: all 5 pass.
cd "$(dirname "$0")/../sample_repo" || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
python3 -m pytest -q -p no:cacheprovider
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed or errored. Read the lines that start with FAILED or ERROR above: they say which test and why."; fi
