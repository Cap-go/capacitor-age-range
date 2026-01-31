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
   * On Android: queries Google Play Age Signals API (no user prompt).
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
   * On Android: 'SUPERVISED' (guardian-managed) or 'VERIFIED' (Google-verified 18+).
   *
   * @since 8.0.0
   */
  declarationSource?: DeclarationSource;

  /**
   * Android-only. The user's Google Play verification status.
   *
   * @since 8.0.0
   */
  androidUserStatus?: AndroidUserStatus;

  /**
   * Android-only. Effective date for the most recent guardian-approved change.
   *
   * @since 8.0.0
   */
  mostRecentApprovalDate?: string;

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
 * @since 8.0.0
 */
export type AndroidUserStatus =
  | 'VERIFIED'
  | 'SUPERVISED'
  | 'SUPERVISED_APPROVAL_PENDING'
  | 'SUPERVISED_APPROVAL_DENIED'
  | 'UNKNOWN'
  | 'EMPTY';
