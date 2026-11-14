package app.capgo.agerange.classes.results;

import static org.junit.Assert.assertEquals;

import app.capgo.agerange.enums.UserStatus;
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus;
import org.junit.Test;

public class RequestAgeRangeResultTest {

    @Test
    public void mapUserStatus_null_returnsEmpty() {
        assertEquals(UserStatus.EMPTY, RequestAgeRangeResult.mapUserStatus(null));
    }

    @Test
    public void mapUserStatus_verified_returnsVerified() {
        assertEquals(UserStatus.VERIFIED, RequestAgeRangeResult.mapUserStatus(AgeSignalsVerificationStatus.VERIFIED));
    }

    @Test
    public void mapUserStatus_supervised_returnsSupervised() {
        assertEquals(UserStatus.SUPERVISED, RequestAgeRangeResult.mapUserStatus(AgeSignalsVerificationStatus.SUPERVISED));
    }

    @Test
    public void mapUserStatus_supervisedApprovalPending_returnsSupervisedApprovalPending() {
        assertEquals(
            UserStatus.SUPERVISED_APPROVAL_PENDING,
            RequestAgeRangeResult.mapUserStatus(AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING)
        );
    }

    @Test
    public void mapUserStatus_supervisedApprovalDenied_returnsSupervisedApprovalDenied() {
        assertEquals(
            UserStatus.SUPERVISED_APPROVAL_DENIED,
            RequestAgeRangeResult.mapUserStatus(AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED)
        );
    }

    @Test
    public void mapUserStatus_unknown_returnsUnknown() {
        assertEquals(UserStatus.UNKNOWN, RequestAgeRangeResult.mapUserStatus(AgeSignalsVerificationStatus.UNKNOWN));
    }

    @Test
    public void mapUserStatus_unrecognizedInteger_returnsEmpty() {
        assertEquals(UserStatus.EMPTY, RequestAgeRangeResult.mapUserStatus(999));
    }
}
