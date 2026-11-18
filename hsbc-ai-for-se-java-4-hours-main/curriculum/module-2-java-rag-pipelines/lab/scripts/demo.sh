#!/usr/bin/env bash
# DEMO: asks one question. Retrieval always runs; the answer step needs ANTHROPIC_API_KEY.
# usage: bash scripts/demo.sh What time is the cutoff for same-day settlement?
cd "$(dirname "$0")/.." || exit 1
Q="$*"; [ -z "$Q" ] && Q="What time is the cutoff for same-day settlement?"
mvn -q compile exec:java -Dexec.mainClass=com.ga.hsbc.module2.RagDemo -Dexec.args="$Q"
