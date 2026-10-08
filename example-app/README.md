# Age Range example app

Vite + TypeScript demo for `@capgo/capacitor-age-range`.

From the repository root:

```bash
bun run example:install
bun run example:build
```

Run the dev server from this folder:

```bash
bun run start
```

Sync native platforms after building:

```bash
bunx cap sync
```

## iOS (Declared Age Range)

After `bunx cap sync ios`, enable the Declared Age Range capability on the example App ID and wire entitlements before calling `requestAgeRange()` on device. Follow the **iOS Setup** section in the [plugin README](../README.md) (`App.entitlements`, `CODE_SIGN_ENTITLEMENTS`, and the App ID capability). Without that setup, iOS builds return `NOT_AVAILABLE` or fail signing.
