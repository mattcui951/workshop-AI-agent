#!/usr/bin/env bash
# TEST SECURITY: runs the security tests for one finding, or all of them.
#   bash scripts/test-security.sh sql       -> SQL injection (core)
#   bash scripts/test-security.sh mask      -> account number masking (core)
#   bash scripts/test-security.sh secrets   -> hard-coded secrets (bonus)
#   bash scripts/test-security.sh pickle    -> unsafe pickle loading (bonus)
#   bash scripts/test-security.sh hash      -> password hashing (bonus)
#   bash scripts/test-security.sh all       -> all 11 tests
cd "$(dirname "$0")/.." || exit 1
# Use the course Python environment if it is not already active.
if [ -z "${VIRTUAL_ENV:-}" ] && [ -f "$HOME/ga-python-venv/bin/activate" ]; then . "$HOME/ga-python-venv/bin/activate"; fi
case "${1:-}" in
  sql)     K="normal_lookup or injection or stacked" ;;
  mask)    K="masks or logs_never" ;;
  secrets) K="secrets" ;;
  pickle)  K="session or malicious" ;;
  hash)    K="password" ;;
  all)     K="" ;;
  *) echo "Say which tests: sql, mask, secrets, pickle, hash or all.  Example: bash scripts/test-security.sh sql"; exit 1 ;;
esac
if [ -n "$K" ]; then python3 -m pytest -q -p no:cacheprovider tests/test_security.py -k "$K"; else python3 -m pytest -q -p no:cacheprovider tests/test_security.py; fi
if [ $? -eq 0 ]; then echo; echo "PASSED: every test in this step is green."; else echo; echo "NOT YET: some tests failed or errored. Read the lines that start with FAILED or ERROR above: they say which test and why."; fi
