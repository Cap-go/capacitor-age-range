package app.capgo.agerange.classes.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import app.capgo.agerange.enums.UserStatus;
import com.google.android.play.agesignals.AgeSignalsResult;
import com.google.android.play.agesignals.model.AgeRangeSource;
import com.google.android.play.agesignals.model.AgeSignalsStatus;
import com.google.android.play.agesignals.model.SignificantChangeStatus;
import java.util.Date;
import org.junit.Test;

public class RequestAgeRangeResultTest {

    @Test
    public void mapAgeRangeSource_tierValues() {
        assertEquals("TIER_A", RequestAgeRangeResult.mapAgeRangeSource(AgeRangeSource.TIER_A));
        assertEquals("TIER_B", RequestAgeRangeResult.mapAgeRangeSource(AgeRangeSource.TIER_B));
        assertEquals("TIER_C", RequestAgeRangeResult.mapAgeRangeSource(AgeRangeSource.TIER_C));
        assertEquals("TIER_D", RequestAgeRangeResult.mapAgeRangeSource(AgeRangeSource.TIER_D));
        assertEquals("UNSPECIFIED", RequestAgeRangeResult.mapAgeRangeSource(AgeRangeSource.UNSPECIFIED));
        assertNull(RequestAgeRangeResult.mapAgeRangeSource(null));
    }

    @Test
    public void mapSignificantChangeStatus_values() {
        assertEquals("APPROVED", RequestAgeRangeResult.mapSignificantChangeStatus(SignificantChangeStatus.APPROVED));
        assertEquals("PENDING", RequestAgeRangeResult.mapSignificantChangeStatus(SignificantChangeStatus.PENDING));
        assertEquals("DECLINED", RequestAgeRangeResult.mapSignificantChangeStatus(SignificantChangeStatus.DECLINED));
        assertEquals("UNSPECIFIED", RequestAgeRangeResult.mapSignificantChangeStatus(SignificantChangeStatus.UNSPECIFIED));
    }

    @Test
    public void mapLegacyAndroidUserStatus_supervisedTierMapsToSupervisedFamily() {
        assertEquals(UserStatus.SUPERVISED, RequestAgeRangeResult.mapLegacyAndroidUserStatus(AgeRangeSource.TIER_B, null, 13, 15));
        assertEquals(
            UserStatus.SUPERVISED_APPROVAL_PENDING,
            RequestAgeRangeResult.mapLegacyAndroidUserStatus(AgeRangeSource.TIER_B, SignificantChangeStatus.PENDING, 13, 15)
        );
        assertEquals(
            UserStatus.SUPERVISED_APPROVAL_DENIED,
            RequestAgeRangeResult.mapLegacyAndroidUserStatus(AgeRangeSource.TIER_B, SignificantChangeStatus.DECLINED, 13, 15)
        );
    }

    @Test
    public void mapLegacyAndroidUserStatus_verifiedTiers() {
        assertEquals(UserStatus.VERIFIED, RequestAgeRangeResult.mapLegacyAndroidUserStatus(AgeRangeSource.TIER_D, null, 18, null));
        assertEquals(UserStatus.VERIFIED, RequestAgeRangeResult.mapLegacyAndroidUserStatus(AgeRangeSource.TIER_C, null, 18, null));
    }

    @Test
    public void tierDResult_mapsToSharingWithNewFields() {
        AgeSignalsResult signals = AgeSignalsResult.builder()
            .setAgeRangeSource(AgeRangeSource.TIER_D)
            .setAgeLower(18)
            .setAgeUpper(null)
            .build();

        RequestAgeRangeResult result = new RequestAgeRangeResult(signals);

        assertEquals("SHARING", result.statusForTests());
        assertEquals("VERIFIED", result.declarationSourceForTests());
        assertEquals("TIER_D", result.ageRangeSourceForTests());
        assertEquals(UserStatus.VERIFIED, result.androidUserStatusForTests());
        assertEquals(Integer.valueOf(18), result.ageLowerForTests());
        assertNull(result.ageUpperForTests());
    }

    @Test
    public void tierBSupervisedMinor_includesSignificantChangeFields() {
        Date approved = new Date(1262304000000L);
        AgeSignalsResult signals = AgeSignalsResult.builder()
            .setAgeRangeSource(AgeRangeSource.TIER_B)
            .setAgeLower(13)
            .setAgeUpper(15)
            .setSignificantChangeStatus(SignificantChangeStatus.APPROVED)
            .setSignificantChangeApprovalDate(approved)
            .setInstallId("install-abc")
            .build();

        RequestAgeRangeResult result = new RequestAgeRangeResult(signals);

        assertEquals("SHARING", result.statusForTests());
        assertEquals("GUARDIAN_DECLARED", result.declarationSourceForTests());
        assertEquals(UserStatus.SUPERVISED, result.androidUserStatusForTests());
        assertEquals("APPROVED", result.significantChangeStatusForTests());
        assertNotNull(result.significantChangeApprovalDateForTests());
        assertEquals("install-abc", result.installIdForTests());
    }

    @Test
    public void declinedSharing_nullAgeRangeSource() {
        AgeSignalsResult signals = AgeSignalsResult.builder().setAgeRangeSource(null).setAgeLower(null).setAgeUpper(null).build();

        RequestAgeRangeResult result = new RequestAgeRangeResult(signals);

        assertEquals("DECLINED_SHARING", result.statusForTests());
        assertEquals(UserStatus.UNKNOWN, result.androidUserStatusForTests());
    }

    @Test
    public void fromAgeSignalsAccessStatus_notShared() {
        RequestAgeRangeResult result = RequestAgeRangeResult.fromAgeSignalsAccessStatus(AgeSignalsStatus.NOT_SHARED);

        assertEquals("DECLINED_SHARING", result.statusForTests());
        assertEquals("NOT_SHARED", result.androidAgeSignalsAccessStatusForTests());
    }

    @Test
    public void fromAgeSignalsAccessStatus_verificationRequired() {
        RequestAgeRangeResult result = RequestAgeRangeResult.fromAgeSignalsAccessStatus(AgeSignalsStatus.VERIFICATION_REQUIRED);

        assertEquals("NOT_AVAILABLE", result.statusForTests());
        assertEquals("VERIFICATION_REQUIRED", result.androidAgeSignalsAccessStatusForTests());
    }
}
