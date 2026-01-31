# capacitor-age-range
  <a href="https://capgo.app/"><img src='https://raw.githubusercontent.com/Cap-go/capgo/main/assets/capgo_banner.png' alt='Capgo - Instant updates for capacitor'/></a>

<div align="center">
  <h2><a href="https://capgo.app/?ref=plugin_age_range"> ➡️ Get Instant updates for your App with Capgo</a></h2>
  <h2><a href="https://capgo.app/consulting/?ref=plugin_age_range"> Missing a feature? We'll build the plugin for you 💪</a></h2>
</div>

Cross-platform age range detection for Capacitor apps. Uses **Google Play Age Signals** on Android and **Apple DeclaredAgeRange** on iOS.

## Why Capacitor Age Range?

A unified, **free**, and **lightweight** age range plugin:

- **Cross-platform** - Single API for both Android and iOS
- **Privacy-preserving** - No birth dates collected, only age ranges
- **Regulatory compliance** - Meet COPPA, KOSA, and regional age-appropriate design requirements
- **Automatic mapping** - Platform-specific responses mapped to a unified format
- **Modern package management** - Supports both Swift Package Manager (SPM) and CocoaPods
- **Zero dependencies** - Minimal footprint beyond platform SDKs

## Documentation

The most complete doc is available here: https://capgo.app/docs/plugins/age-range/

## Compatibility

| Plugin version | Capacitor compatibility | Maintained |
| -------------- | ----------------------- | ---------- |
| v8.\*.\*       | v8.\*.\*                | ✅          |
| v7.\*.\*       | v7.\*.\*                | On demand   |
| v6.\*.\*       | v6.\*.\*                | ❌          |
| v5.\*.\*       | v5.\*.\*                | ❌          |

> **Note:** The major version of this plugin follows the major version of Capacitor. Use the version that matches your Capacitor installation (e.g., plugin v8 for Capacitor 8). Only the latest major version is actively maintained.

## Install

```bash
npm install @capgo/capacitor-age-range
npx cap sync
```

## iOS Setup

### Requirements

- iOS 15+ (plugin compiles and runs, returns `NOT_AVAILABLE` on older versions)
- iOS 26+ for actual age range functionality (DeclaredAgeRange framework)
- Xcode 26+

### Entitlement

Add the `com.apple.developer.declared-age-range` entitlement to your app:

1. In Xcode, select your target → **Signing & Capabilities**
2. Click **+ Capability** → search for **Declared Age Range**
3. Enable it

Or add manually to your `*.entitlements` file:

```xml
<key>com.apple.developer.declared-age-range</key>
<true/>
```

### How it works

On iOS, `requestAgeRange()` presents a system dialog where the user (or their guardian via Family Sharing) declares their age range. The `ageGates` option controls the age boundaries shown in the dialog (default: `[13, 16, 18]`).

## Android Setup

### Requirements

- Android API 24+ (minSdk)
- Google Play Store installed and up to date

### How it works

On Android, the plugin queries **Google Play Age Signals API** in the background — no user prompt is shown. The Play Store determines the user's age verification status from their Google account.

No additional permissions or manifest changes are needed.

## Web

Not supported. The web implementation throws `'AgeRange does not have web implementation'`.

## API

<docgen-index>

* [`requestAgeRange(...)`](#requestagerange)
* [`getPluginVersion()`](#getpluginversion)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Cross-platform age range detection plugin.

Uses Google Play Age Signals on Android and Apple DeclaredAgeRange on iOS.

### requestAgeRange(...)

```typescript
requestAgeRange(options?: RequestAgeRangeOptions | undefined) => Promise<AgeRangeResult>
```

Request the user's age range.

On Android: queries Google Play Age Signals API (no user prompt).
On iOS: presents the system DeclaredAgeRange dialog (requires iOS 26.2+).

| Param         | Type                                                                      | Description                               |
| ------------- | ------------------------------------------------------------------------- | ----------------------------------------- |
| **`options`** | <code><a href="#requestagerangeoptions">RequestAgeRangeOptions</a></code> | - Configuration for the age range request |

**Returns:** <code>Promise&lt;<a href="#agerangeresult">AgeRangeResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### getPluginVersion()

```typescript
getPluginVersion() => Promise<{ version: string; }>
```

Get the native Capacitor plugin version.

**Returns:** <code>Promise&lt;{ version: string; }&gt;</code>

**Since:** 8.0.0

--------------------


### Interfaces


#### AgeRangeResult

Result of the age range request.

| Prop                         | Type                                                            | Description                                                                                                                                                           | Since |
| ---------------------------- | --------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| **`status`**                 | <code><a href="#agerangestatus">AgeRangeStatus</a></code>       | The outcome status of the age range request.                                                                                                                          | 8.0.0 |
| **`ageLower`**               | <code>number</code>                                             | Inclusive lower bound of the user's age range. Present when age data is available.                                                                                    | 8.0.0 |
| **`ageUpper`**               | <code>number</code>                                             | Inclusive upper bound of the user's age range. May be absent if the user is in the highest age bracket (e.g. 18+).                                                    | 8.0.0 |
| **`declarationSource`**      | <code><a href="#declarationsource">DeclarationSource</a></code> | How the age was declared/determined. On iOS: 'SELF_DECLARED' or 'GUARDIAN_DECLARED'. On Android: 'SUPERVISED' (guardian-managed) or 'VERIFIED' (Google-verified 18+). | 8.0.0 |
| **`androidUserStatus`**      | <code><a href="#androiduserstatus">AndroidUserStatus</a></code> | Android-only. The user's Google Play verification status.                                                                                                             | 8.0.0 |
| **`mostRecentApprovalDate`** | <code>string</code>                                             | Android-only. Effective date for the most recent guardian-approved change.                                                                                            | 8.0.0 |
| **`installId`**              | <code>string</code>                                             | Android-only. Install identifier for supervised installs in Google Play.                                                                                              | 8.0.0 |


#### RequestAgeRangeOptions

Options for the age range request.

| Prop           | Type                  | Description                                                                                                                                                                                                                                                                              | Default                   | Since |
| -------------- | --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------- | ----- |
| **`ageGates`** | <code>number[]</code> | Age thresholds for the request. On iOS: these are passed to `requestAgeRange(ageGates:)` as the age boundaries presented in the system dialog. Common values: [13, 16, 18]. On Android: this parameter is ignored (Play Age Signals returns predefined ranges: 0-12, 13-15, 16-17, 18+). | <code>[13, 16, 18]</code> | 8.0.0 |


### Type Aliases


#### AgeRangeStatus

Top-level status of the age range request.

<code>'SHARING' | 'DECLINED_SHARING' | 'NOT_AVAILABLE' | 'ERROR'</code>


#### DeclarationSource

How the age range was declared or determined.

<code>'SELF_DECLARED' | 'GUARDIAN_DECLARED' | 'VERIFIED' | 'UNKNOWN'</code>


#### AndroidUserStatus

Android-specific Google Play user status values.

<code>'VERIFIED' | 'SUPERVISED' | 'SUPERVISED_APPROVAL_PENDING' | 'SUPERVISED_APPROVAL_DENIED' | 'UNKNOWN' | 'EMPTY'</code>

</docgen-api>

## Usage

```typescript
import { AgeRange } from '@capgo/capacitor-age-range';

// Request age range with default gates (13, 16, 18)
const result = await AgeRange.requestAgeRange();

switch (result.status) {
  case 'SHARING':
    console.log('Age range:', result.ageLower, '-', result.ageUpper);
    console.log('Source:', result.declarationSource);
    break;
  case 'DECLINED_SHARING':
    console.log('User declined to share age');
    break;
  case 'NOT_AVAILABLE':
    console.log('Age range API not available');
    break;
  case 'ERROR':
    console.log('Error occurred');
    break;
}

// Custom age gates (iOS only)
const result2 = await AgeRange.requestAgeRange({ ageGates: [13, 18] });
```

## Platform Response Mapping

### Android (Google Play Age Signals)

| Android UserStatus              | → status           | → declarationSource |
| ------------------------------- | ------------------ | ------------------- |
| VERIFIED                        | SHARING            | VERIFIED            |
| SUPERVISED                      | SHARING            | GUARDIAN_DECLARED   |
| SUPERVISED_APPROVAL_PENDING     | SHARING            | GUARDIAN_DECLARED   |
| SUPERVISED_APPROVAL_DENIED      | SHARING            | GUARDIAN_DECLARED   |
| UNKNOWN / EMPTY                 | DECLINED_SHARING   | —                   |

### iOS (DeclaredAgeRange)

| iOS Response                    | → status           | → declarationSource |
| ------------------------------- | ------------------ | ------------------- |
| .sharing (selfDeclared)         | SHARING            | SELF_DECLARED       |
| .sharing (guardianDeclared)     | SHARING            | GUARDIAN_DECLARED   |
| .declinedSharing                | DECLINED_SHARING   | —                   |
