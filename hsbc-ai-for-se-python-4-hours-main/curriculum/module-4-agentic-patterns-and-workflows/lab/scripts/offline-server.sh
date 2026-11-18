#!/usr/bin/env bash
# INSTRUCTOR ONLY: replays a scripted conversation when there is no API key or no network. It is NOT a model.
#   bash scripts/offline-server.sh            (see the speaker notes for the second terminal)
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
python3 rehearsal_server.py "$@"
