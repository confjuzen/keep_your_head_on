#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$ROOT_DIR"

chmod +x ./gradlew || true

echo "==> Generating Javadoc for all modules"
./gradlew --no-daemon javadoc javadocAll

echo
echo "==> Documentation generated at:"
if [ -d "build/docs/javadoc-all" ]; then
  echo " - Aggregated: $(pwd)/build/docs/javadoc-all/index.html"
fi
if [ -d "core/build/docs/javadoc" ]; then
  echo " - Core:       $(pwd)/core/build/docs/javadoc/index.html"
fi
if [ -d "lwjgl3/build/docs/javadoc" ]; then
  echo " - LWJGL3:     $(pwd)/lwjgl3/build/docs/javadoc/index.html"
fi

echo "\n==> Done."
