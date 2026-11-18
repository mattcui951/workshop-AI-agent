#!/usr/bin/env bash
# BONUS: starts the Vert.x endpoint on http://localhost:8080 (needs web/RagServer.java and ANTHROPIC_API_KEY).
# Stop it with Ctrl+C.
cd "$(dirname "$0")/.." || exit 1
if [ ! -f src/main/java/com/ga/hsbc/module2/web/RagServer.java ]; then
  echo "web/RagServer.java does not exist yet. Write it (bonus step) or run: bash scripts/catch-up.sh server"; exit 1
fi
mvn -q compile exec:java -Dexec.mainClass=com.ga.hsbc.module2.web.RagServer
