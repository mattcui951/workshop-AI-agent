#!/usr/bin/env bash
# SCAN: runs the bandit security scanner on audit_target.
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
python3 -m bandit -q -r audit_target
