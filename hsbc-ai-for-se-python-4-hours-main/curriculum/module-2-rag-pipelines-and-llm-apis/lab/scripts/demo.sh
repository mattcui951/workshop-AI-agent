#!/usr/bin/env bash
# DEMO: asks one question. Retrieval always runs; the answer step needs ANTHROPIC_API_KEY.
# usage: bash scripts/demo.sh What time is the cutoff for same-day settlement?
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
Q="$*"; [ -z "$Q" ] && Q="What time is the cutoff for same-day settlement?"
python3 ask_cli.py "$Q"
