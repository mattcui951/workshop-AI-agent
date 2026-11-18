#!/usr/bin/env bash
# CHECK KEY: tells you whether the Claude API key is set in this terminal. It never prints the key itself.
if [ -z "$ANTHROPIC_API_KEY" ]; then
  echo "NO KEY: ANTHROPIC_API_KEY is not set in this terminal."
  echo "You can still do every test step. Only the live demo needs a key. Ask your instructor if you need one."
  exit 1
fi
echo "KEY FOUND: ANTHROPIC_API_KEY is set (it ends in ...${ANTHROPIC_API_KEY: -4})."
echo "Model: ${CLAUDE_MODEL:-claude-sonnet-5-5 (default)}"
