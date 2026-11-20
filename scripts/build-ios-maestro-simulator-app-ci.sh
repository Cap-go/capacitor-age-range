#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

export CAPGO_MAESTRO_CI=1

cd "$repo_root/example-app"

if [ ! -d "$repo_root/dist/esm" ]; then
  (cd "$repo_root" && bun run build)
fi

bun install --frozen-lockfile --ignore-scripts
bun run build
if [ ! -d ios ]; then
  bunx cap add ios
fi
bunx cap sync ios

info_plist="ios/App/App/Info.plist"
if [ -f "$info_plist" ]; then
  /usr/libexec/PlistBuddy -c "Delete :CapgoMaestroCiUnavailable" "$info_plist" 2>/dev/null || true
  /usr/libexec/PlistBuddy -c "Add :CapgoMaestroCiUnavailable bool true" "$info_plist"
fi

xcodebuild \
  -project ios/App/App.xcodeproj \
  -scheme App \
  -destination "generic/platform=iOS Simulator" \
  -derivedDataPath ios/DerivedData \
  -quiet \
  build \
  CODE_SIGNING_ALLOWED=NO