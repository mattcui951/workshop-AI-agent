#!/usr/bin/env bash
# RUN PR REVIEW: the agent reads sample_repo, finds the bug and proposes a patch.
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
unset AUTO_APPROVE
python3 pr_review_app.py sample_repo
