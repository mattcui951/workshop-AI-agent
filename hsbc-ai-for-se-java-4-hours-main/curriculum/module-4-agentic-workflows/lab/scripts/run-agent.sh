#!/usr/bin/env bash
# RUN AGENT: the function-calling agent. It asks you before EVERY tool call. Type y or n and press Enter.
#   bash scripts/run-agent.sh                       -> the default question (settled gold)
#   bash scripts/run-agent.sh Your own question     -> any question you type (used in the capstone)
cd "$(dirname "$0")/.." || exit 1
unset AUTO_APPROVE
if [ $# -gt 0 ]; then
  mvn -q compile exec:java -Dexec.mainClass=com.ga.hsbc.module4.AgentApp -Dexec.args="$*"
else
  mvn -q compile exec:java -Dexec.mainClass=com.ga.hsbc.module4.AgentApp
fi
