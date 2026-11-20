#!/usr/bin/env bash
set -euo pipefail

if [ -z "${IOS_SIMULATOR_UDID:-}" ]; then
  echo "IOS_SIMULATOR_UDID is not set"
  exit 1
fi

app_bundle="${1:-}"
if [ -z "$app_bundle" ]; then
  if [ -d example-app/ios/DerivedData/Build/Products/Debug-iphonesimulator/App.app ]; then
    app_bundle="example-app/ios/DerivedData/Build/Products/Debug-iphonesimulator/App.app"
  elif [ -d maestro-artifacts/App.app ]; then
    app_bundle="maestro-artifacts/App.app"
  else
    app_bundle="$(find maestro-artifacts -type d -name '*.app' 2>/dev/null | head -n 1 || true)"
  fi
fi

if [ -z "$app_bundle" ] || [ ! -d "$app_bundle" ]; then
  app_bundle="$(find . -path './example-app/ios/DerivedData/*' -type d -name 'App.app' 2>/dev/null | head -n 1 || true)"
fi

if [ -z "$app_bundle" ] || [ ! -d "$app_bundle" ]; then
  echo "Simulator app bundle not found under maestro-artifacts"
  if [ -d maestro-artifacts ]; then
    find maestro-artifacts -maxdepth 4 -print 2>/dev/null | head -n 40 || true
  fi
  exit 1
fi

xcrun simctl boot "$IOS_SIMULATOR_UDID" 2>/dev/null || true
if ! perl -e 'alarm shift; exec @ARGV' 75 xcrun simctl bootstatus "$IOS_SIMULATOR_UDID" -b; then
  echo "Simulator bootstatus capped at 75s; continuing."
fi

xcrun simctl install "$IOS_SIMULATOR_UDID" "$app_bundle"
open -a Simulator --args -CurrentDeviceUDID "$IOS_SIMULATOR_UDID"