#!/usr/bin/env bash
# BENCHMARK: times the legacy code against your refactor. Your numbers will differ from your neighbour's.
cd "$(dirname "$0")/../refactor" || exit 1
mvn -q test-compile exec:java -Dexec.mainClass=com.ga.hsbc.module3.recon.BenchmarkDemo -Dexec.classpathScope=test
