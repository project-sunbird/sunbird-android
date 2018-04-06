package org.sunbird.notification;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.sunbird.models.Notification;
import org.sunbird.telemetry.TelemetryConstant;
import org.sunbird.utils.Constants;
import org.sunbird.utils.SerializableUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 8/2/2016.
 *
 * @author anil
 */
public class LocalNotificationService extends IntentService {

    private static final String TAG = LocalNotificationService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public LocalNotificationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        if (intent != null) {
            Notification genieNotification = null;

            byte[] bytes = intent.getByteArrayExtra(Constants.BUNDLE_KEY_NOTIFICATION_DATA_MODEL);
            if (bytes != null) {
                genieNotification = SerializableUtil.deserialize(bytes);
            }

            if (genieNotification != null) {
                Map<String, Object> eksMap = new HashMap<>();
                eksMap.put(TelemetryConstant.NOTIFICATION_DATA, GsonUtil.toJson(genieNotification));
//                eksMap.put(TelemetryConstant.NOTIFICATION_MSG_ID,  String.valueOf(genieNotification.getMsgid()));
                if (genieNotification.getRelativetime() > 0) { // Do nothing.
//                    PreferenceUtil.setOnBoardingNotificationState(genieNotification.getRelativetime());
//                    TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.OTHER, TelemetryPageId.LOCAL_NOTIFICATION, TelemetryAction.NOTIFICATION_DISPLAYED, String.valueOf(genieNotification.getMsgid()), eksMap));
//                    LocalBroadcastManager.getInstance(this).sendBroadcast(Util.getRefreshNotificationsIntent());
                } else {
//                    Log.d(TelemetryAction.NOTIFICATION_DISPLAYED, "onHandleIntent: "+TelemetryPageId.SERVER_NOTIFICATION);
                    //No longer sending the telemetry
                    //TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.OTHER, TelemetryAction.NOTIFICATION_DISPLAYED, TelemetryPageId.SERVER_NOTIFICATION, ContextEnvironment.HOME, eksMap));
                }

                NotificationManagerUtil notificationManagerUtil = new NotificationManagerUtil(LocalNotificationService.this);
                notificationManagerUtil.handleNotificationAction(genieNotification);
            }
        }
    }

}
