#!/usr/bin/env bash
# CATCH UP: copies a finished fix into this lab so you can keep going.
#   bash scripts/catch-up.sh refactor | sql | mask | secrets | pickle | hash
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
case "${1:-}" in
  refactor) cp $S/reconciler.py . ;;
  sql)      cp $S/audit_target/repository.py audit_target/ ;;
  mask)     cp $S/audit_target/exporter.py audit_target/ ;;
  secrets)  cp $S/audit_target/config.py audit_target/ ;;
  pickle)   cp $S/audit_target/session_cache.py audit_target/ ;;
  hash)     cp $S/audit_target/auth.py audit_target/ ;;
  *) echo "Say what to catch up on: refactor, sql, mask, secrets, pickle or hash"; exit 1 ;;
esac && echo "Copied the finished '$1' code."
