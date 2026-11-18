#!/usr/bin/env bash
# CATCH UP: copies the finished files into this lab so you can keep going.
#   bash scripts/catch-up.sh          -> models.py (the core task)
#   bash scripts/catch-up.sh stubs    -> app.py and tests/test_app_stubs.py (bonus)
cd "$(dirname "$0")/.." || exit 1
# Where the finished code comes from: the instructor copy has ../solutions; the learner copy carries a packed
# version in scripts/.catch-up-pack (made by resources/prepare-delivery.sh when it removes solutions/).
SOLROOT=../solutions
if [ ! -d "$SOLROOT" ] && [ -f scripts/.catch-up-pack ]; then
  SOLROOT="$(mktemp -d)"; trap 'rm -rf "$SOLROOT"' EXIT
  # Quiet harmless warnings about Mac/Windows file metadata when GNU tar unpacks the pack.
  TARX="tar -xz"; tar --version 2>/dev/null | grep -q GNU && TARX="tar --warning=no-unknown-keyword -xz"
  base64 -d scripts/.catch-up-pack | $TARX -C "$SOLROOT" || { echo "Could not unpack the catch-up files. Ask your instructor."; exit 1; }
fi
S=$SOLROOT
if [ ! -d "$S" ]; then echo "The catch-up files are not on this machine. Ask your instructor."; exit 1; fi
case "${1:-models}" in
  models) cp "$S/models.py" . && echo "Copied models.py. Now run: bash scripts/check-contract.sh" ;;
  stubs)  cp "$S/app.py" . && cp "$S/tests/test_app_stubs.py" tests/ && echo "Copied app.py and tests/test_app_stubs.py. Run: python3 -m pytest -q tests/test_app_stubs.py" ;;
  *) echo "Use: bash scripts/catch-up.sh   or   bash scripts/catch-up.sh stubs" ;;
esac
