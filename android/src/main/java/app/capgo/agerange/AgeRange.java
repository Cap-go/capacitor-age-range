package app.capgo.agerange;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.capgo.agerange.classes.CustomExceptions;
import app.capgo.agerange.classes.results.RequestAgeRangeResult;
import app.capgo.agerange.interfaces.NonEmptyResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.play.agesignals.AgeSignalsAccessRequest;
import com.google.android.play.agesignals.AgeSignalsManager;
import com.google.android.play.agesignals.AgeSignalsManagerFactory;
import com.google.android.play.agesignals.AgeSignalsRequest;
import com.google.android.play.agesignals.AgeSignalsResult;
import com.google.android.play.agesignals.ApplicableAgeFeaturesRequest;
import com.google.android.play.agesignals.model.AgeSignalsStatus;
import com.google.android.play.agesignals.model.ApplicableAgeFeature;

public class AgeRange {

    public static final String ACCESS_NEVER = "never";
    public static final String ACCESS_ALWAYS = "always";
    public static final String ACCESS_WHEN_MANDATORY = "whenMandatory";

    @NonNull
    private final AgeRangePlugin plugin;

    public AgeRange(@NonNull AgeRangePlugin plugin) {
        this.plugin = plugin;
    }

    public void requestAgeRange(@NonNull String androidAgeSignalsAccess, @NonNull NonEmptyResultCallback<RequestAgeRangeResult> callback) {
        AgeSignalsManager manager = AgeSignalsManagerFactory.create(plugin.getActivity());
        String accessMode = normalizeAccessMode(androidAgeSignalsAccess);

        switch (accessMode) {
            case ACCESS_ALWAYS:
                requestAccessThenCheck(manager, callback);
                break;
            case ACCESS_WHEN_MANDATORY:
                manager
                    .getApplicableAgeFeatures(ApplicableAgeFeaturesRequest.builder().build())
                    .addOnSuccessListener((featuresResult) -> {
                        boolean mandatory = featuresResult.features().contains(ApplicableAgeFeature.MANDATORY_SHARING);
                        if (mandatory) {
                            requestAccessThenCheck(manager, callback);
                        } else {
                            checkAgeSignalsOnly(manager, callback);
                        }
                    })
                    .addOnFailureListener((exception) -> callback.error(mapErrorToException(exception)));
                break;
            case ACCESS_NEVER:
            default:
                checkAgeSignalsOnly(manager, callback);
                break;
        }
    }

    @NonNull
    private String normalizeAccessMode(@NonNull String accessMode) {
        if (ACCESS_ALWAYS.equals(accessMode) || ACCESS_WHEN_MANDATORY.equals(accessMode)) {
            return accessMode;
        }
        return ACCESS_NEVER;
    }

    private void requestAccessThenCheck(
        @NonNull AgeSignalsManager manager,
        @NonNull NonEmptyResultCallback<RequestAgeRangeResult> callback
    ) {
        AgeSignalsAccessRequest accessRequest = AgeSignalsAccessRequest.builder().setActivity(plugin.getActivity()).build();

        manager
            .requestAgeSignalsAccess(accessRequest)
            .addOnSuccessListener((accessResult) -> {
                Integer accessStatus = accessResult.ageSignalsStatus();
                if (accessStatus != null && accessStatus == AgeSignalsStatus.SHARED) {
                    checkAgeSignalsOnly(manager, callback, accessStatus);
                    return;
                }
                try {
                    callback.success(RequestAgeRangeResult.fromAgeSignalsAccessStatus(accessStatus));
                } catch (Exception exception) {
                    callback.error(exception);
                }
            })
            .addOnFailureListener((exception) -> callback.error(mapErrorToException(exception)));
    }

    private void checkAgeSignalsOnly(@NonNull AgeSignalsManager manager, @NonNull NonEmptyResultCallback<RequestAgeRangeResult> callback) {
        checkAgeSignalsOnly(manager, callback, null);
    }

    private void checkAgeSignalsOnly(
        @NonNull AgeSignalsManager manager,
        @NonNull NonEmptyResultCallback<RequestAgeRangeResult> callback,
        @Nullable Integer ageSignalsAccessStatus
    ) {
        AgeSignalsRequest request = AgeSignalsRequest.builder().build();

        Task<AgeSignalsResult> task = manager.checkAgeSignals(request);
        task.addOnSuccessListener((ageSignalsResult) -> {
            try {
                RequestAgeRangeResult result = new RequestAgeRangeResult(ageSignalsResult, ageSignalsAccessStatus);
                callback.success(result);
            } catch (Exception exception) {
                callback.error(exception);
            }
        });
        task.addOnFailureListener((exception) -> {
            Exception mapped = mapErrorToException(exception);
            callback.error(mapped);
        });
    }

    @NonNull
    private Exception mapErrorToException(@NonNull Exception exception) {
        if (!(exception instanceof com.google.android.gms.common.api.ApiException)) {
            return exception;
        }

        com.google.android.gms.common.api.ApiException apiException = (com.google.android.gms.common.api.ApiException) exception;
        int statusCode = apiException.getStatusCode();

        switch (statusCode) {
            case 25000:
                return CustomExceptions.API_NOT_AVAILABLE;
            case 25001:
                return CustomExceptions.PLAY_STORE_NOT_FOUND;
            case 25002:
                return CustomExceptions.NETWORK_ERROR;
            case 25003:
                return CustomExceptions.PLAY_SERVICES_NOT_FOUND;
            case 25004:
                return CustomExceptions.CANNOT_BIND_TO_SERVICE;
            case 25005:
                return CustomExceptions.PLAY_STORE_VERSION_OUTDATED;
            case 25006:
                return CustomExceptions.PLAY_SERVICES_VERSION_OUTDATED;
            case 25007:
                return CustomExceptions.CLIENT_TRANSIENT_ERROR;
            case 25008:
                return CustomExceptions.APP_NOT_OWNED;
            case 25009:
                return CustomExceptions.INTERNAL_ERROR;
            default:
                return exception;
        }
    }
}
