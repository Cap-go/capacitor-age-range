package app.capgo.agerange;

import androidx.annotation.NonNull;
import com.getcapacitor.Logger;
import com.google.android.gms.tasks.Task;
import com.google.android.play.agesignals.AgeSignalsManager;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Reflection bridge for {@code getApplicableAgeFeatures()} (Play Age Signals 0.0.6+).
 * Keeps the plugin compiling when {@code androidPlayAgeSignalsVersion} is 0.0.4.
 */
final class PlayAgeSignalsMandatorySharing {

    private static final String TAG = "AgeRange";
    private static final String APPLICABLE_AGE_FEATURES_REQUEST = "com.google.android.play.agesignals.ApplicableAgeFeaturesRequest";
    private static final String APPLICABLE_AGE_FEATURE = "com.google.android.play.agesignals.model.ApplicableAgeFeature";
    private static final String MANDATORY_SHARING = "MANDATORY_SHARING";

    private PlayAgeSignalsMandatorySharing() {}

    interface MandatoryCheckCallback {
        void onResult(boolean mandatorySharing);

        void onFallback();
    }

    static void queryMandatorySharing(@NonNull AgeSignalsManager manager, @NonNull MandatoryCheckCallback callback) {
        try {
            Class<?> requestClass = Class.forName(APPLICABLE_AGE_FEATURES_REQUEST);
            Object request = buildFeaturesRequest(requestClass);
            Method getApplicableAgeFeatures = manager.getClass().getMethod("getApplicableAgeFeatures", requestClass);
            @SuppressWarnings("unchecked")
            Task<Object> task = (Task<Object>) getApplicableAgeFeatures.invoke(manager, request);
            task.addOnSuccessListener((featuresResult) -> {
                try {
                    boolean mandatory = featuresContainMandatorySharing(featuresResult);
                    callback.onResult(mandatory);
                } catch (ReflectiveOperationException exception) {
                    Logger.warn(TAG, "Could not read applicable age features, using checkAgeSignals only: " + exception.getMessage());
                    callback.onFallback();
                }
            });
            task.addOnFailureListener((exception) -> {
                Logger.warn(TAG, "getApplicableAgeFeatures failed, using checkAgeSignals only: " + exception.getMessage());
                callback.onFallback();
            });
        } catch (ReflectiveOperationException exception) {
            Logger.debug(TAG, "getApplicableAgeFeatures not available on this Play Age Signals version");
            callback.onFallback();
        }
    }

    @NonNull
    private static Object buildFeaturesRequest(@NonNull Class<?> requestClass) throws ReflectiveOperationException {
        Object builder = requestClass.getMethod("builder").invoke(null);
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private static boolean featuresContainMandatorySharing(@NonNull Object featuresResult) throws ReflectiveOperationException {
        Method featuresMethod = featuresResult.getClass().getMethod("features");
        Object featuresObject = featuresMethod.invoke(featuresResult);
        if (!(featuresObject instanceof Collection<?>)) {
            return false;
        }
        Collection<?> features = (Collection<?>) featuresObject;
        Class<?> featureClass = Class.forName(APPLICABLE_AGE_FEATURE);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Object mandatoryFeature = Enum.valueOf((Class) featureClass, MANDATORY_SHARING);
        return features.contains(mandatoryFeature);
    }
}
