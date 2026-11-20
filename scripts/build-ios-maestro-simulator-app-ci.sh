#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
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

xcodebuild \
  -project ios/App/App.xcodeproj \
  -scheme App \
  -destination "generic/platform=iOS Simulator" \
  -derivedDataPath ios/DerivedData \
  -quiet \
  build \
  CODE_SIGNING_ALLOWED=NO