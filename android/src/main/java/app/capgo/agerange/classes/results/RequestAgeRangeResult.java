package app.capgo.agerange.classes.results;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.capgo.agerange.enums.UserStatus;
import app.capgo.agerange.interfaces.Result;
import com.getcapacitor.JSObject;
import com.google.android.play.agesignals.AgeSignalsResult;

public class RequestAgeRangeResult implements Result {

    @NonNull
    private final UserStatus userStatus;

    @Nullable
    private final Integer ageLower;

    @Nullable
    private final Integer ageUpper;

    @Nullable
    private final String mostRecentApprovalDate;

    @Nullable
    private final String installId;

    public RequestAgeRangeResult(@NonNull AgeSignalsResult ageSignalsResult) {
        this.userStatus = mapUserStatus(ageSignalsResult.userStatus().toString());
        this.ageLower = ageSignalsResult.ageLower();
        this.ageUpper = ageSignalsResult.ageUpper();
        this.mostRecentApprovalDate = ageSignalsResult.mostRecentApprovalDate() != null
            ? ageSignalsResult.mostRecentApprovalDate().toString()
            : null;
        this.installId = ageSignalsResult.installId();
    }

    @Override
    @NonNull
    public JSObject toJSObject() {
        JSObject result = new JSObject();

        // Map to unified status
        switch (userStatus) {
            case VERIFIED:
                result.put("status", "SHARING");
                result.put("ageLower", 18);
                result.put("declarationSource", "VERIFIED");
                break;
            case SUPERVISED:
            case SUPERVISED_APPROVAL_PENDING:
            case SUPERVISED_APPROVAL_DENIED:
                result.put("status", "SHARING");
                result.put("declarationSource", "GUARDIAN_DECLARED");
                if (ageLower != null) {
                    result.put("ageLower", ageLower);
                }
                if (ageUpper != null) {
                    result.put("ageUpper", ageUpper);
                }
                break;
            default:
                result.put("status", "DECLINED_SHARING");
                break;
        }

        // Always include Android-specific fields
        result.put("androidUserStatus", userStatus.name());

        if (mostRecentApprovalDate != null) {
            result.put("mostRecentApprovalDate", mostRecentApprovalDate);
        }
        if (installId != null) {
            result.put("installId", installId);
        }

        return result;
    }

    @NonNull
    private UserStatus mapUserStatus(@NonNull String status) {
        switch (status) {
            case "VERIFIED":
                return UserStatus.VERIFIED;
            case "SUPERVISED":
                return UserStatus.SUPERVISED;
            case "SUPERVISED_APPROVAL_PENDING":
                return UserStatus.SUPERVISED_APPROVAL_PENDING;
            case "SUPERVISED_APPROVAL_DENIED":
                return UserStatus.SUPERVISED_APPROVAL_DENIED;
            case "UNKNOWN":
                return UserStatus.UNKNOWN;
            default:
                return UserStatus.EMPTY;
        }
    }
}
