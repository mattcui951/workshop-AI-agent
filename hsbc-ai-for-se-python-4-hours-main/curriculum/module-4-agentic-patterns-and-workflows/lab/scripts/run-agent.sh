#!/usr/bin/env bash
# RUN AGENT: the function-calling agent. It asks you before EVERY tool call. Type y or n and press Enter.
#   bash scripts/run-agent.sh                     -> the default question
#   bash scripts/run-agent.sh Your own question   -> any question you type (used in the capstone)
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
unset AUTO_APPROVE
python3 agent_app.py "$@"
