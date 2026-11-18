#!/usr/bin/env bash
# CATCH UP: copies the finished qa_service.py (build_prompt and ask) into this lab so you can keep going.
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
cp "$S/qa_service.py" . && echo "Copied qa_service.py. Now run: bash scripts/test-core.sh"
