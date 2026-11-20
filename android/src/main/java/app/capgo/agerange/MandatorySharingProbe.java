package app.capgo.agerange;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.android.play.agesignals.AgeSignalsManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Calls {@code getApplicableAgeFeatures()} via reflection so the plugin compiles against Play Age Signals 0.0.4+.
 * The method exists only from 0.0.6 onward.
 */
final class MandatorySharingProbe {

    private MandatorySharingProbe() {}

    static void resolve(
        @NonNull AgeSignalsManager manager,
        @NonNull Runnable onMandatory,
        @NonNull Runnable onNotMandatory,
        @NonNull Runnable onUnavailable
    ) {
        try {
            Class<?> requestClass = buildFeaturesRequestClass();
            Method getFeatures = manager.getClass().getMethod("getApplicableAgeFeatures", requestClass);
            Object request = buildFeaturesRequest();
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
                    Method featuresMethod = featuresResult.getClass().getMethod("features");
                    @SuppressWarnings("unchecked")
                    Set<Integer> features = (Set<Integer>) featuresMethod.invoke(featuresResult);
                    int mandatoryFlag = resolveMandatorySharingFlag();
                    if (features != null && features.contains(mandatoryFlag)) {
                        onMandatory.run();
                    } else {
                        onNotMandatory.run();
                    }
                } catch (ReflectiveOperationException reflectiveOperationException) {
                    onNotMandatory.run();
                }
            });
            featuresTask.addOnFailureListener((exception) -> onNotMandatory.run());
        } catch (ReflectiveOperationException reflectiveOperationException) {
            onUnavailable.run();
        }
    }

    @NonNull
    private static Class<?> buildFeaturesRequestClass() throws ClassNotFoundException {
        return Class.forName("com.google.android.play.agesignals.ApplicableAgeFeaturesRequest");
    }

    @NonNull
    private static Object buildFeaturesRequest() throws ReflectiveOperationException {
        Class<?> requestClass = buildFeaturesRequestClass();
        Method builderMethod = requestClass.getMethod("builder");
        Object builder = builderMethod.invoke(null);
        Method buildMethod = builder.getClass().getMethod("build");
        return buildMethod.invoke(builder);
    }

    private static int resolveMandatorySharingFlag() throws ReflectiveOperationException {
        Class<?> featureClass = Class.forName("com.google.android.play.agesignals.model.ApplicableAgeFeature");
        Field mandatoryField = featureClass.getField("MANDATORY_SHARING");
        return mandatoryField.getInt(null);
    }
}
