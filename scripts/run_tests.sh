#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$ROOT_DIR"

# Ensure Gradle wrapper is executable
chmod +x ./gradlew || true

echo "==> Cleaning, running unit tests, and generating coverage reports (JaCoCo)"
./gradlew --no-daemon clean test jacocoTestReport

# Find and summarize JaCoCo XML reports for each subproject
echo "\n==> Coverage summary (LINE coverage)"
shopt -s nullglob
found_any=false
for xml in $(find . -type f -path "*/build/reports/jacoco/test/jacocoTestReport.xml"); do
  found_any=true
  module=$(dirname "$xml")
  module=$(dirname "$module")
  module=$(dirname "$module")
  module_name=$(basename "$module")
  # Parse missed/covered counts from XML
  line=$(grep 'counter type="LINE"' "$xml" | head -n1 || true)
  if [[ -n "$line" ]]; then
    missed=$(echo "$line" | sed -n 's/.*missed="\([0-9]\+\)".*/\1/p')
    covered=$(echo "$line" | sed -n 's/.*covered="\([0-9]\+\)".*/\1/p')
    total=$(( missed + covered ))
    if [[ "$total" -gt 0 ]]; then
      pct=$(awk -v c="$covered" -v t="$total" 'BEGIN { printf "%.2f", (c*100.0)/t }')
      echo " - $module_name: $covered/$total lines covered (${pct}%)"
    else
      echo " - $module_name: no lines measured"
    fi
  else
    echo " - $module_name: no LINE counter found in jacocoTestReport.xml"
  fi
  echo "   Report: $(dirname "$xml")/../html/index.html"
  echo "           $xml"
 done

if ! $found_any; then
  echo "No JaCoCo reports found. Did tests run?"
fi

echo "\n==> Done."
