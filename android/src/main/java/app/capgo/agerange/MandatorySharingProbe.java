package app.capgo.agerange;

import androidx.annotation.NonNull;
import com.getcapacitor.Logger;
import com.google.android.gms.tasks.Task;
import com.google.android.play.agesignals.AgeSignalsManager;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Calls {@code getApplicableAgeFeatures()} via reflection so the plugin compiles against Play Age Signals 0.0.4+.
 * The method exists only from 0.0.6 onward.
 */
final class MandatorySharingProbe {

    private static final String MANDATORY_SHARING = "MANDATORY_SHARING";

    private MandatorySharingProbe() {}

    static void resolve(
        @NonNull AgeSignalsManager manager,
        @NonNull Runnable onMandatory,
        @NonNull Runnable onNotMandatory,
        @NonNull Runnable onUnavailable
    ) {
        try {
            Class<?> requestClass = Class.forName("com.google.android.play.agesignals.ApplicableAgeFeaturesRequest");
            Object request = buildFeaturesRequest(requestClass);
            Method getFeatures = manager.getClass().getMethod("getApplicableAgeFeatures", requestClass);
            Object task = getFeatures.invoke(manager, request);
            if (!(task instanceof Task)) {
                onUnavailable.run();
                return;
            }
            @SuppressWarnings("unchecked")
            Task<Object> featuresTask = (Task<Object>) task;
            featuresTask.addOnSuccessListener((featuresResult) -> {
                if (featuresResult == null) {
                    onNotMandatory.run();
                    return;
                }
                try {
                    if (featuresContainMandatorySharing(featuresResult)) {
                        onMandatory.run();
                    } else {
                        onNotMandatory.run();
                    }
                } catch (ReflectiveOperationException | RuntimeException exception) {
                    Logger.warn(
                        AgeRangePlugin.TAG,
                        "Could not read applicable age features, using checkAgeSignals only: " + exception.getMessage()
                    );
                    onUnavailable.run();
                }
            });
            featuresTask.addOnFailureListener((exception) -> {
                Logger.warn(AgeRangePlugin.TAG, "getApplicableAgeFeatures failed, using checkAgeSignals only: " + exception.getMessage());
                onUnavailable.run();
            });
        } catch (ReflectiveOperationException reflectiveOperationException) {
            onUnavailable.run();
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
        Class<?> featureClass = Class.forName("com.google.android.play.agesignals.model.ApplicableAgeFeature");
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Object mandatoryFeature = Enum.valueOf((Class) featureClass, MANDATORY_SHARING);
        return features.contains(mandatoryFeature);
    }
}
