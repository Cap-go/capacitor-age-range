## Maestro

End-to-end flows for the example app on Android emulators and the iOS Simulator.

### Covered in CI

- **Default access (`never`)**: calls `checkAgeSignals()` without `requestAgeSignalsAccess()`. CI asserts `code=API_NOT_AVAILABLE` on the harness detail line (Play Age Signals not available on emulators).
- **`whenMandatory` on Android**: exercises reflection/fallback for `getApplicableAgeFeatures()`, then `checkAgeSignals()`. CI asserts `status=NOT_AVAILABLE` on the resolved plugin result.
- **iOS Simulator**: runs the real `DeclaredAgeRange` path (no plugin or Info.plist test bypass). CI accepts `status=NOT_AVAILABLE` or `status=ERROR` depending on simulator OS and example-app entitlements.

### Not runnable in CI

- The real Google Play in-app age signals sharing prompt (`requestAgeSignalsAccess()` with user consent).
- Verified tiers from a signed Play Store install (TIER_C/D production responses).

### Run locally

```bash
bun run example-app:prepare:android
bun run maestro:test:android:default-never
```