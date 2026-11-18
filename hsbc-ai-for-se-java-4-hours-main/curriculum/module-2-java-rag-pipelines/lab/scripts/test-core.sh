#!/usr/bin/env bash
# TEST CORE: runs the tests for the two methods you write (PromptBuilder.build and RagService.ask).
cd "$(dirname "$0")/.." || exit 1
mvn -q test -Dtest=PromptBuilderTest,RagServiceTest
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed. Read the lines that start with [ERROR] above: they say which test and why."; fi
