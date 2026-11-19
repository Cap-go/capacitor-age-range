/**
 * Cross-platform age range detection plugin.
 *
 * Uses Google Play Age Signals on Android and Apple DeclaredAgeRange on iOS.
 *
 * @since 8.0.0
 */
export interface AgeRangePlugin {
  /**
   * Request the user's age range.
   *
   * On Android: queries Google Play Age Signals API. By default no in-app prompt is shown;
   * set `androidAgeSignalsAccess` to request sharing access when needed.
   * On iOS: presents the system DeclaredAgeRange dialog (requires iOS 26.2+).
   *
   * @param options - Configuration for the age range request
   * @returns Promise that resolves with the age range result
   * @since 8.0.0
   * @example
   * ```typescript
   * const result = await AgeRange.requestAgeRange({ ageGates: [13, 16, 18] });
   * if (result.status === 'SHARING') {
   *   console.log('Age range:', result.ageLower, '-', result.ageUpper);
   * }
   * ```
   */
  requestAgeRange(options?: RequestAgeRangeOptions): Promise<AgeRangeResult>;

  /**
   * Get the native Capacitor plugin version.
   *
   * @returns Promise that resolves with the plugin version
   * @since 8.0.0
   * @example
   * ```typescript
   * const { version } = await AgeRange.getPluginVersion();
   * console.log('Plugin version:', version);
   * ```
   */
  getPluginVersion(): Promise<{ version: string }>;
}

/**
 * Options for the age range request.
 *
 * @since 8.0.0
 */
export interface RequestAgeRangeOptions {
  /**
   * Age thresholds for the request.
   *
   * On iOS: these are passed to `requestAgeRange(ageGates:)` as the
   * age boundaries presented in the system dialog. Common values: [13, 16, 18].
   *
   * On Android: this parameter is ignored (Play Age Signals returns
   * predefined ranges: 0-12, 13-15, 16-17, 18+).
   *
   * @default [13, 16, 18]
   * @since 8.0.0
   */
  ageGates?: number[];

  /**
   * Android-only. Whether to call `requestAgeSignalsAccess()` before `checkAgeSignals()`.
   *
   * - `never` (default): calls `checkAgeSignals()` only, matching pre-0.0.4 behavior with no in-app sharing prompt.
   * - `always`: always requests access first (Google may show the sharing prompt when applicable).
   * - `whenMandatory`: requests access only when `getApplicableAgeFeatures()` reports `MANDATORY_SHARING`.
   *
   * @default 'never'
   * @since 8.3.0
   */
  androidAgeSignalsAccess?: AndroidAgeSignalsAccessMode;
}

/**
 * Result of the age range request.
 *
 * @since 8.0.0
 */
export interface AgeRangeResult {
  /**
   * The outcome status of the age range request.
   *
   * @since 8.0.0
   */
  status: AgeRangeStatus;

  /**
   * Inclusive lower bound of the user's age range.
   *
   * Present when age data is available.
   *
   * @since 8.0.0
   */
  ageLower?: number;

  /**
   * Inclusive upper bound of the user's age range.
   *
   * May be absent if the user is in the highest age bracket (e.g. 18+).
   *
   * @since 8.0.0
   */
  ageUpper?: number;

  /**
   * How the age was declared/determined.
   *
   * On iOS: 'SELF_DECLARED' or 'GUARDIAN_DECLARED'.
   * On Android: derived from `ageRangeSource` (TIER_A self-declared, TIER_B guardian-managed, TIER_C/TIER_D verified).
   *
   * @since 8.0.0
   */
  declarationSource?: DeclarationSource;

  /**
   * Android-only. Legacy mapping from Play Age Signals 0.0.2 `userStatus()`.
   *
   * Play Age Signals 0.0.4+ removed this field. The plugin derives approximate values from
   * `ageRangeSource` and `significantChangeStatus` for backward compatibility. Prefer those fields.
   *
   * @deprecated since 8.3.0. Use `ageRangeSource` and `significantChangeStatus` instead.
   * @since 8.0.0
   */
  androidUserStatus?: AndroidUserStatus;

  /**
   * Android-only. How Google classified the shared age range (Play Age Signals 0.0.4+).
   *
   * @since 8.3.0
   */
  ageRangeSource?: AndroidAgeRangeSource;

  /**
   * Android-only. Approval status for significant account changes in applicable jurisdictions.
   *
   * @since 8.3.0
   */
  significantChangeStatus?: AndroidSignificantChangeStatus;

  /**
   * Android-only. Effective date of the most recently approved significant change.
   *
   * @since 8.3.0
   */
  significantChangeApprovalDate?: string;

  /**
   * Android-only. Alias for `significantChangeApprovalDate` (Play 0.0.2 field name).
   *
   * @deprecated since 8.3.0. Use `significantChangeApprovalDate`.
   * @since 8.0.0
   */
  mostRecentApprovalDate?: string;

  /**
   * Android-only. Result of `requestAgeSignalsAccess()` when `androidAgeSignalsAccess` is not `never`.
   *
   * @since 8.3.0
   */
  androidAgeSignalsAccessStatus?: AndroidAgeSignalsAccessStatus;

  /**
   * Android-only. Install identifier for supervised installs in Google Play.
   *
   * @since 8.0.0
   */
  installId?: string;
}

/**
 * Top-level status of the age range request.
 *
 * @since 8.0.0
 */
export type AgeRangeStatus =
  /**
   * The user shared their age range (iOS) or age signals are available (Android).
   */
  | 'SHARING'
  /**
   * The user declined to share their age range.
   */
  | 'DECLINED_SHARING'
  /**
   * The age range API is not available on this device/OS version.
   */
  | 'NOT_AVAILABLE'
  /**
   * An error occurred while requesting the age range.
   */
  | 'ERROR';

/**
 * How the age range was declared or determined.
 *
 * @since 8.0.0
 */
export type DeclarationSource =
  /** The user self-declared their age (iOS). */
  | 'SELF_DECLARED'
  /** A guardian declared the user's age (iOS Family Sharing or Android supervised). */
  | 'GUARDIAN_DECLARED'
  /** Google has verified the user is 18+ (Android only). */
  | 'VERIFIED'
  /** Source is unknown or not provided by the platform. */
  | 'UNKNOWN';

/**
 * Android-specific Google Play user status values.
 *
 * @deprecated since 8.3.0. Mapped from `ageRangeSource` / `significantChangeStatus` on Play Age Signals 0.0.4+.
 * @since 8.0.0
 */
export type AndroidUserStatus =
  | 'VERIFIED'
  | 'SUPERVISED'
  | 'SUPERVISED_APPROVAL_PENDING'
  | 'SUPERVISED_APPROVAL_DENIED'
  | 'UNKNOWN'
  | 'EMPTY';

/**
 * Android-only. Controls the Play Age Signals access request flow.
 *
 * @since 8.3.0
 */
export type AndroidAgeSignalsAccessMode = 'never' | 'always' | 'whenMandatory';

/**
 * Android-only. Play Age Signals `ageRangeSource` tier (API 0.0.4+).
 *
 * @since 8.3.0
 */
export type AndroidAgeRangeSource = 'UNSPECIFIED' | 'TIER_A' | 'TIER_B' | 'TIER_C' | 'TIER_D';

/**
 * Android-only. Play Age Signals `significantChangeStatus` (API 0.0.4+).
 *
 * @since 8.3.0
 */
export type AndroidSignificantChangeStatus = 'UNSPECIFIED' | 'APPROVED' | 'PENDING' | 'DECLINED';

/**
 * Android-only. Play Age Signals access status from `requestAgeSignalsAccess()`.
 *
 * @since 8.3.0
 */
export type AndroidAgeSignalsAccessStatus = 'UNSPECIFIED' | 'SHARED' | 'NOT_SHARED' | 'VERIFICATION_REQUIRED';
