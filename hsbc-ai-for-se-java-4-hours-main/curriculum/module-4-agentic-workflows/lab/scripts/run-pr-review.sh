#!/usr/bin/env bash
# RUN PR REVIEW: the agent reads sample-service, finds the bug and proposes a patch.
cd "$(dirname "$0")/.." || exit 1
unset AUTO_APPROVE
mvn -q compile exec:java -Dexec.mainClass=com.ga.hsbc.module4.PrReviewApp -Dexec.args="sample-service"
