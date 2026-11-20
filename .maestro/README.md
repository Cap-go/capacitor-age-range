## Maestro

End-to-end flows for the example app on Android emulators and the iOS Simulator.

### Covered in CI

- **Default access (`never`)**: calls `checkAgeSignals()` without `requestAgeSignalsAccess()`. On emulators this usually finishes with `NOT_AVAILABLE` or a Play Services error, which is the expected unavailable path.
- **`whenMandatory` on Android**: exercises reflection/fallback for `getApplicableAgeFeatures()`. Emulators without mandatory sharing still complete via `checkAgeSignals()`.
- **iOS Simulator**: `requestAgeRange()` without Declared Age Range support returns `NOT_AVAILABLE`.

### Not runnable in CI

- The real Google Play in-app age signals sharing prompt (`requestAgeSignalsAccess()` with user consent).
- Verified tiers from a signed Play Store install (TIER_C/D production responses).

### Run locally

```bash
bun run example-app:prepare:android
bun run maestro:test:android:default-never
```