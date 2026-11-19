package app.capgo.agerange.classes.results;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.capgo.agerange.enums.UserStatus;
import app.capgo.agerange.interfaces.Result;
import com.getcapacitor.JSObject;
import com.google.android.play.agesignals.AgeSignalsResult;
import com.google.android.play.agesignals.model.AgeRangeSource;
import com.google.android.play.agesignals.model.AgeSignalsStatus;
import com.google.android.play.agesignals.model.SignificantChangeStatus;

public class RequestAgeRangeResult implements Result {

    @NonNull
    private final String status;

    @Nullable
    private final Integer ageLower;

    @Nullable
    private final Integer ageUpper;

    @Nullable
    private final String declarationSource;

    @Nullable
    private final UserStatus androidUserStatus;

    @Nullable
    private final String ageRangeSource;

    @Nullable
    private final String significantChangeStatus;

    @Nullable
    private final String significantChangeApprovalDate;

    @Nullable
    private final String installId;

    @Nullable
    private final String androidAgeSignalsAccessStatus;

    private RequestAgeRangeResult(
        @NonNull String status,
        @Nullable Integer ageLower,
        @Nullable Integer ageUpper,
        @Nullable String declarationSource,
        @Nullable UserStatus androidUserStatus,
        @Nullable String ageRangeSource,
        @Nullable String significantChangeStatus,
        @Nullable String significantChangeApprovalDate,
        @Nullable String installId,
        @Nullable String androidAgeSignalsAccessStatus
    ) {
        this.status = status;
        this.ageLower = ageLower;
        this.ageUpper = ageUpper;
        this.declarationSource = declarationSource;
        this.androidUserStatus = androidUserStatus;
        this.ageRangeSource = ageRangeSource;
        this.significantChangeStatus = significantChangeStatus;
        this.significantChangeApprovalDate = significantChangeApprovalDate;
        this.installId = installId;
        this.androidAgeSignalsAccessStatus = androidAgeSignalsAccessStatus;
    }

    public RequestAgeRangeResult(@NonNull AgeSignalsResult ageSignalsResult) {
        Integer ageRangeSourceValue = ageSignalsResult.ageRangeSource();
        Integer significantChangeStatusValue = ageSignalsResult.significantChangeStatus();
        Integer ageLowerValue = ageSignalsResult.ageLower();
        Integer ageUpperValue = ageSignalsResult.ageUpper();
        String approvalDate =
            ageSignalsResult.significantChangeApprovalDate() != null ? ageSignalsResult.significantChangeApprovalDate().toString() : null;
        UserStatus legacyUserStatus = mapLegacyAndroidUserStatus(
            ageRangeSourceValue,
            significantChangeStatusValue,
            ageLowerValue,
            ageUpperValue
        );
        boolean sharing = hasSharedAgeRange(ageLowerValue, ageUpperValue, ageRangeSourceValue);

        if (sharing) {
            this.status = "SHARING";
            this.ageLower = ageLowerValue;
            this.ageUpper = ageUpperValue;
            this.declarationSource = mapDeclarationSource(ageRangeSourceValue, ageLowerValue, ageUpperValue);
        } else {
            this.status = "DECLINED_SHARING";
            this.ageLower = null;
            this.ageUpper = null;
            this.declarationSource = null;
        }

        this.androidUserStatus = legacyUserStatus;
        this.ageRangeSource = mapAgeRangeSource(ageRangeSourceValue);
        this.significantChangeStatus = mapSignificantChangeStatus(significantChangeStatusValue);
        this.significantChangeApprovalDate = approvalDate;
        this.installId = ageSignalsResult.installId();
        this.androidAgeSignalsAccessStatus = null;
    }

    @NonNull
    public static RequestAgeRangeResult fromAgeSignalsAccessStatus(@Nullable Integer ageSignalsStatus) {
        int resolvedStatus = ageSignalsStatus != null ? ageSignalsStatus : AgeSignalsStatus.UNSPECIFIED;
        String unifiedStatus;
        String accessStatusLabel = mapAgeSignalsAccessStatus(resolvedStatus);

        switch (resolvedStatus) {
            case AgeSignalsStatus.NOT_SHARED:
                unifiedStatus = "DECLINED_SHARING";
                break;
            case AgeSignalsStatus.VERIFICATION_REQUIRED:
                unifiedStatus = "NOT_AVAILABLE";
                break;
            default:
                unifiedStatus = "DECLINED_SHARING";
                break;
        }

        return new RequestAgeRangeResult(unifiedStatus, null, null, null, UserStatus.UNKNOWN, null, null, null, null, accessStatusLabel);
    }

    @Override
    @NonNull
    public JSObject toJSObject() {
        JSObject result = new JSObject();
        result.put("status", status);

        if (ageLower != null) {
            result.put("ageLower", ageLower);
        }
        if (ageUpper != null) {
            result.put("ageUpper", ageUpper);
        }
        if (declarationSource != null) {
            result.put("declarationSource", declarationSource);
        }
        if (androidUserStatus != null) {
            result.put("androidUserStatus", androidUserStatus.name());
        }
        if (ageRangeSource != null) {
            result.put("ageRangeSource", ageRangeSource);
        }
        if (significantChangeStatus != null) {
            result.put("significantChangeStatus", significantChangeStatus);
        }
        if (significantChangeApprovalDate != null) {
            result.put("significantChangeApprovalDate", significantChangeApprovalDate);
            result.put("mostRecentApprovalDate", significantChangeApprovalDate);
        }
        if (installId != null) {
            result.put("installId", installId);
        }
        if (androidAgeSignalsAccessStatus != null) {
            result.put("androidAgeSignalsAccessStatus", androidAgeSignalsAccessStatus);
        }

        return result;
    }

    @Nullable
    static String mapAgeRangeSource(@Nullable Integer source) {
        if (source == null) {
            return null;
        }
        switch (source) {
            case AgeRangeSource.TIER_A:
                return "TIER_A";
            case AgeRangeSource.TIER_B:
                return "TIER_B";
            case AgeRangeSource.TIER_C:
                return "TIER_C";
            case AgeRangeSource.TIER_D:
                return "TIER_D";
            case AgeRangeSource.UNSPECIFIED:
            default:
                return "UNSPECIFIED";
        }
    }

    @Nullable
    static String mapSignificantChangeStatus(@Nullable Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case SignificantChangeStatus.APPROVED:
                return "APPROVED";
            case SignificantChangeStatus.PENDING:
                return "PENDING";
            case SignificantChangeStatus.DECLINED:
                return "DECLINED";
            case SignificantChangeStatus.UNSPECIFIED:
            default:
                return "UNSPECIFIED";
        }
    }

    @Nullable
    static String mapAgeSignalsAccessStatus(int status) {
        switch (status) {
            case AgeSignalsStatus.SHARED:
                return "SHARED";
            case AgeSignalsStatus.NOT_SHARED:
                return "NOT_SHARED";
            case AgeSignalsStatus.VERIFICATION_REQUIRED:
                return "VERIFICATION_REQUIRED";
            case AgeSignalsStatus.UNSPECIFIED:
            default:
                return "UNSPECIFIED";
        }
    }

    @Nullable
    static String mapDeclarationSource(@Nullable Integer ageRangeSource, @Nullable Integer ageLower, @Nullable Integer ageUpper) {
        if (ageRangeSource == null) {
            return "UNKNOWN";
        }
        switch (ageRangeSource) {
            case AgeRangeSource.TIER_D:
            case AgeRangeSource.TIER_C:
                return "VERIFIED";
            case AgeRangeSource.TIER_B:
                return "GUARDIAN_DECLARED";
            case AgeRangeSource.TIER_A:
                return "SELF_DECLARED";
            case AgeRangeSource.UNSPECIFIED:
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Maps Play Age Signals 0.0.4+ fields to the legacy {@code androidUserStatus} values removed from the SDK.
     *
     * @deprecated Consumers should migrate to {@code ageRangeSource} and {@code significantChangeStatus}.
     */
    @Nullable
    static UserStatus mapLegacyAndroidUserStatus(
        @Nullable Integer ageRangeSource,
        @Nullable Integer significantChangeStatus,
        @Nullable Integer ageLower,
        @Nullable Integer ageUpper
    ) {
        if (!hasSharedAgeRange(ageLower, ageUpper, ageRangeSource)) {
            return UserStatus.UNKNOWN;
        }
        if (ageRangeSource == null || ageRangeSource == AgeRangeSource.UNSPECIFIED) {
            return UserStatus.UNKNOWN;
        }
        switch (ageRangeSource) {
            case AgeRangeSource.TIER_D:
            case AgeRangeSource.TIER_C:
                return UserStatus.VERIFIED;
            case AgeRangeSource.TIER_B:
                if (significantChangeStatus != null) {
                    switch (significantChangeStatus) {
                        case SignificantChangeStatus.PENDING:
                            return UserStatus.SUPERVISED_APPROVAL_PENDING;
                        case SignificantChangeStatus.DECLINED:
                            return UserStatus.SUPERVISED_APPROVAL_DENIED;
                        default:
                            break;
                    }
                }
                return UserStatus.SUPERVISED;
            case AgeRangeSource.TIER_A:
            default:
                if (isVerifiedAdultTier(ageRangeSource, ageLower, ageUpper)) {
                    return UserStatus.VERIFIED;
                }
                return UserStatus.UNKNOWN;
        }
    }

    static boolean hasSharedAgeRange(@Nullable Integer ageLower, @Nullable Integer ageUpper, @Nullable Integer ageRangeSource) {
        if (ageRangeSource == null || ageRangeSource == AgeRangeSource.UNSPECIFIED) {
            return false;
        }
        if (ageLower != null || ageUpper != null) {
            return true;
        }
        return isVerifiedAdultTier(ageRangeSource, ageLower, ageUpper);
    }

    static boolean isVerifiedAdultTier(@Nullable Integer ageRangeSource, @Nullable Integer ageLower, @Nullable Integer ageUpper) {
        if (ageRangeSource == null) {
            return false;
        }
        if (ageRangeSource == AgeRangeSource.TIER_C || ageRangeSource == AgeRangeSource.TIER_D) {
            return ageLower != null && ageLower >= 18 && ageUpper == null;
        }
        return ageRangeSource == AgeRangeSource.TIER_A && ageLower != null && ageLower >= 18 && ageUpper == null;
    }

    @NonNull
    String statusForTests() {
        return status;
    }

    @Nullable
    Integer ageLowerForTests() {
        return ageLower;
    }

    @Nullable
    Integer ageUpperForTests() {
        return ageUpper;
    }

    @Nullable
    String declarationSourceForTests() {
        return declarationSource;
    }

    @Nullable
    UserStatus androidUserStatusForTests() {
        return androidUserStatus;
    }

    @Nullable
    String ageRangeSourceForTests() {
        return ageRangeSource;
    }

    @Nullable
    String significantChangeStatusForTests() {
        return significantChangeStatus;
    }

    @Nullable
    String significantChangeApprovalDateForTests() {
        return significantChangeApprovalDate;
    }

    @Nullable
    String installIdForTests() {
        return installId;
    }

    @Nullable
    String androidAgeSignalsAccessStatusForTests() {
        return androidAgeSignalsAccessStatus;
    }
}
