#!/usr/bin/env bash
# CATCH UP: copies finished code into this lab so you can keep going.
#   bash scripts/catch-up.sh gate      -> the fixed make_gate (pr_review_app.py)
#   bash scripts/catch-up.sh service   -> the fixed portfolio.py, applied by hand (keeps a .bak)
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
F=sample_repo/portfolio.py
case "${1:-}" in
  gate)    cp $S/pr_review_app.py . && echo "Copied. Now run: bash scripts/test-gate.sh" ;;
  service) [ -f "$F.bak" ] || cp "$F" "$F.bak"; cp $S/portfolio_fixed.py "$F" && echo "Applied the fix. Now run: bash scripts/test-service.sh" ;;
  *) echo "Say what to catch up on: gate or service"; exit 1 ;;
esac
