#!/usr/bin/env bash
# CHECK CONTRACT: compiles your records and checks they match openapi.yaml.
cd "$(dirname "$0")/.." || exit 1
echo "Checking that your Java records match openapi.yaml..."
mvn -q test -Dtest=OpenApiContractTest
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed. Read the lines that start with [ERROR] above: they say which test and why."; fi
