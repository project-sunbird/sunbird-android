package org.sunbird.telemetry;

import android.util.Log;

import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.commons.IResponseHandler;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.telemetry.Telemetry;


public class TelemetryHandler {

    private static final String TAG = TelemetryHandler.class.getSimpleName();

    public static void saveTelemetry(Telemetry event, IResponseHandler handler) {
        GenieService.getAsyncService().getTelemetryService().saveTelemetry(event, handler);
    }

    public static void saveTelemetry(Telemetry event) {
        GenieService.getAsyncService().getTelemetryService().saveTelemetry(event, new IResponseHandler<Void>() {
            @Override
            public void onSuccess(GenieResponse<Void> genieResponse) {
                Log.i(TAG, "TelemetryEvent sent successfully");
            }

            @Override
            public void onError(GenieResponse<Void> genieResponse) {
                Log.e(TAG, "TelemetryEvent sending Failed");
            }
        });
    }

    public static void saveTelemetry(String event) {
        GenieService.getAsyncService().getTelemetryService().saveTelemetry(event, new IResponseHandler<Void>() {
            @Override
            public void onSuccess(GenieResponse<Void> genieResponse) {
                Log.i(TAG, "TelemetryEvent sent successfully");
            }

            @Override
            public void onError(GenieResponse<Void> genieResponse) {
                Log.e(TAG, "TelemetryEvent sending Failed");
            }
        });
    }

}
