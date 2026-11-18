#!/usr/bin/env bash
# CATCH UP: copies finished code into this lab so you can keep going.
#   bash scripts/catch-up.sh core     -> PromptBuilder and RagService (the core task)
#   bash scripts/catch-up.sh server   -> the bonus Vert.x endpoint, web/RagServer.java
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
SRC=$SOLROOT/src/main/java/com/ga/hsbc/module2
DST=src/main/java/com/ga/hsbc/module2
if [ ! -d "$SRC" ]; then echo "The catch-up files are not on this machine. Ask your instructor."; exit 1; fi
case "$1" in
  core)   cp "$SRC"/PromptBuilder.java "$SRC"/RagService.java "$DST"/ && echo "Copied. Now run: bash scripts/test-core.sh" ;;
  server) mkdir -p "$DST"/web && cp "$SRC"/web/RagServer.java "$DST"/web/ && echo "Copied. Now run: bash scripts/start-server.sh" ;;
  *) echo "Say what to catch up on: bash scripts/catch-up.sh core   or   bash scripts/catch-up.sh server" ;;
esac
