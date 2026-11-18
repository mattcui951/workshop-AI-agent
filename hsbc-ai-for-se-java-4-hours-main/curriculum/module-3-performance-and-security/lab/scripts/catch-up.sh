#!/usr/bin/env bash
# CATCH UP: copies a finished fix into this lab so you can keep going.
#   bash scripts/catch-up.sh refactor   -> the refactored TradeReconciler
#   bash scripts/catch-up.sh sql        -> the SQL injection fix (InventoryRepository)
#   bash scripts/catch-up.sh mask       -> the masking fix (StatementExporter)
#   bash scripts/catch-up.sh secrets    -> the secrets fix (AppConfig)
#   bash scripts/catch-up.sh deps       -> the pom.xml without the vulnerable libraries
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
A=src/main/java/com/atlas/inventory
case "$1" in
  refactor) cp $S/refactor/src/main/java/com/ga/hsbc/module3/recon/TradeReconciler.java refactor/src/main/java/com/ga/hsbc/module3/recon/ ;;
  sql)      cp $S/audit-target/$A/InventoryRepository.java audit-target/$A/ ;;
  mask)     cp $S/audit-target/$A/StatementExporter.java audit-target/$A/ ;;
  secrets)  cp $S/audit-target/$A/AppConfig.java audit-target/$A/ ;;
  deps)     cp $S/audit-target/pom.xml audit-target/pom.xml ;;
  *) echo "Say what to catch up on: refactor, sql, mask, secrets or deps"; exit 1 ;;
esac && echo "Copied the finished '$1' code."
