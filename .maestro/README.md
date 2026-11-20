## Maestro

End-to-end flows for the example app on Android emulators and the iOS Simulator.

### Covered in CI

- **Default access (`never`)**: calls `checkAgeSignals()` without `requestAgeSignalsAccess()`. CI asserts the harness label `Maestro default never: status=…` from the real plugin result (iOS Simulator: `NOT_AVAILABLE`; Android emulators often `ERROR` with `API_NOT_AVAILABLE` or similar).
- **`whenMandatory` on Android**: exercises reflection/fallback for `getApplicableAgeFeatures()`. Emulators without mandatory sharing still complete via `checkAgeSignals()`.
- **iOS Simulator**: CI builds set `CapgoMaestroCiUnavailable` in the example `Info.plist` so `requestAgeRange()` returns `NOT_AVAILABLE` without the Declared Age Range sheet (real device builds omit this flag).

### Not runnable in CI

- The real Google Play in-app age signals sharing prompt (`requestAgeSignalsAccess()` with user consent).
- Verified tiers from a signed Play Store install (TIER_C/D production responses).

### Run locally

```bash
bun run example-app:prepare:android
bun run maestro:test:android:default-never
```