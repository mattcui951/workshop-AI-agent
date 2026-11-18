#!/usr/bin/env bash
# BENCHMARK: times the legacy code against your refactor. Your numbers will differ from your neighbour's.
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
python3 benchmark_demo.py
