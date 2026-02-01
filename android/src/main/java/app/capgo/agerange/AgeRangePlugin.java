package app.capgo.agerange;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.capgo.agerange.classes.CustomException;
import app.capgo.agerange.classes.results.RequestAgeRangeResult;
import app.capgo.agerange.interfaces.NonEmptyResultCallback;
import app.capgo.agerange.interfaces.Result;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AgeRange")
public class AgeRangePlugin extends Plugin {

    private final String pluginVersion = "8.1.0";
    public static final String TAG = "AgeRange";
    private static final String ERROR_UNKNOWN = "An unknown error occurred.";

    private AgeRange implementation;

    @Override
    public void load() {
        implementation = new AgeRange(this);
    }

    @PluginMethod
    public void requestAgeRange(@NonNull PluginCall call) {
        try {
            NonEmptyResultCallback<RequestAgeRangeResult> callback = new NonEmptyResultCallback<>() {
                @Override
                public void success(@NonNull RequestAgeRangeResult result) {
                    resolveCall(call, result);
                }

                @Override
                public void error(@NonNull Exception exception) {
                    rejectCall(call, exception);
                }
            };

            if (implementation == null) {
                rejectCall(call, new Exception("Plugin not initialized."));
                return;
            }

            implementation.requestAgeRange(callback);
        } catch (Exception exception) {
            rejectCall(call, exception);
        }
    }

    private void rejectCall(@NonNull PluginCall call, @NonNull Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isEmpty()) {
            message = ERROR_UNKNOWN;
        }
        Logger.error(TAG, message, exception);

        if (exception instanceof CustomException) {
            JSObject error = new JSObject();
            CustomException customException = (CustomException) exception;
            error.put("code", customException.getCode());
            error.put("message", message);
            call.reject(message, customException.getCode(), error);
        } else {
            call.reject(message);
        }
    }

    private void resolveCall(@NonNull PluginCall call, @Nullable Result result) {
        if (result == null) {
            call.resolve();
        } else {
            call.resolve(result.toJSObject());
        }
    }

    @PluginMethod
    public void getPluginVersion(final PluginCall call) {
        try {
            final JSObject ret = new JSObject();
            ret.put("version", this.pluginVersion);
            call.resolve(ret);
        } catch (final Exception e) {
            call.reject("Could not get plugin version", e);
        }
    }
}
