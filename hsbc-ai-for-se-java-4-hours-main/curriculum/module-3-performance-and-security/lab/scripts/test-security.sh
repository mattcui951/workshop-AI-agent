#!/usr/bin/env bash
# TEST SECURITY: runs the security regression tests for one finding, or all of them.
#   bash scripts/test-security.sh sql       -> SQL injection (core)
#   bash scripts/test-security.sh mask      -> account number masking (core)
#   bash scripts/test-security.sh secrets   -> hard-coded secrets (bonus)
#   bash scripts/test-security.sh all       -> all 7 tests
cd "$(dirname "$0")/../audit-target" || exit 1
case "$1" in
  sql)     T='SecurityRegressionTest#normalLookupStillWorks+sqlInjection*' ;;
  mask)    T='SecurityRegressionTest#statementMasksTheAccountNumber+logsNeverContainTheFullAccountNumber' ;;
  secrets) T='SecurityRegressionTest#secretsComeFromTheEnvironmentNotFromSource+missingSecretsFailFastInsteadOfFallingBackToADefault' ;;
  all)     T='SecurityRegressionTest' ;;
  *) echo "Say which tests: sql, mask, secrets or all.  Example: bash scripts/test-security.sh sql"; exit 1 ;;
esac
mvn -q test -Dtest="$T"
if [ $? -eq 0 ]; then echo; echo "PASSED: every test for '$1' is green."; else echo; echo "NOT YET: read the lines that start with [ERROR] above: they say which test and why."; fi
