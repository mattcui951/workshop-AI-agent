#!/usr/bin/env bash
# START OVER: puts THIS lab folder back exactly as it was before you started.
# It deletes every change you made in this lab folder (and only this folder).
# Works from a git clone, and also from a downloaded ZIP copy of the course (it uses scripts/.start-over-pack).
LAB="${START_OVER_LAB:-$(cd "$(dirname "$0")/.." && pwd)}" || exit 1
# Run from a temporary copy, because this script itself gets restored while it runs.
if [ -z "${START_OVER_COPY:-}" ]; then
  TMP_SELF="$(mktemp)"; cp "$0" "$TMP_SELF"
  START_OVER_LAB="$LAB" START_OVER_COPY="$TMP_SELF" exec bash "$TMP_SELF" "$@"
fi
trap 'rm -f "$START_OVER_COPY"' EXIT
cd "$LAB" || exit 1
echo "This will DELETE every change you made in:"
echo "  $LAB"
read -r -p "Type yes (or y) and press Enter to continue: " ok
case "$(echo "$ok" | tr '[:upper:]' '[:lower:]')" in
  y|yes) ;;
  *) echo "Cancelled. Nothing changed. (You pressed Enter without typing yes.)"; exit 0 ;;
esac
if [ -f scripts/.start-over-pack ]; then
  PACK="$(mktemp)"; cp scripts/.start-over-pack "$PACK"
  KEEP="$(mktemp)"; [ -f scripts/.catch-up-pack ] && cp scripts/.catch-up-pack "$KEEP"
  find . -mindepth 1 -maxdepth 1 -exec rm -rf {} +
  # Quiet harmless warnings about Mac/Windows file metadata when GNU tar unpacks the pack.
  TARX="tar -xz"; tar --version 2>/dev/null | grep -q GNU && TARX="tar --warning=no-unknown-keyword -xz"
  base64 -d "$PACK" | $TARX -C . || { echo "Could not restore the lab. Ask your instructor."; exit 1; }
  cp "$PACK" scripts/.start-over-pack; [ -s "$KEEP" ] && cp "$KEEP" scripts/.catch-up-pack
  rm -f "$PACK" "$KEEP"
  echo "Done. This lab is back to its starting state."
elif git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  git checkout -- . && git clean -fdq -- . && echo "Done. This lab is back to its starting state."
else
  echo "This copy has no restore point (not a git clone, and no scripts/.start-over-pack). Ask your instructor for a fresh copy."
  exit 1
fi
