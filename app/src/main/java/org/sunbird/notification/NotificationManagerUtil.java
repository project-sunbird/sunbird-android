package org.sunbird.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.ekstep.genieservices.commons.bean.enums.InteractionType;
import org.ekstep.genieservices.commons.utils.DateUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.sunbird.GlobalApplication;
import org.sunbird.R;
import org.sunbird.models.Notification;
import org.sunbird.notification.enums.NotificationActionId;
import org.sunbird.telemetry.TelemetryAction;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryConstant;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.telemetry.TelemetryPageId;
import org.sunbird.ui.MainActivity;
import org.sunbird.utils.AlarmManagerUtil;
import org.sunbird.utils.Constants;
import org.sunbird.utils.SerializableUtil;
import org.sunbird.utils.Util;

import java.util.HashMap;
import java.util.Map;


/**
 * Singleton class for Notification manager
 *
 * @author anil
 */
public class NotificationManagerUtil {

    private static final String TAG = NotificationManagerUtil.class.getSimpleName();

    private static final int _1_SEC = 1000;
    private static final int _1_MIN = _1_SEC * 60;
    private static final int _1_HOUR = _1_MIN * 60;

    private Context mContext;

    public NotificationManagerUtil(Context context) {
        mContext = context;
    }

    /**
     * start driver location alarm service
     */
    public void handleNotification(final Notification notification) {
        boolean isSchedule = false;

        //Generate GE_INTERACT event for Server notification received.
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(TelemetryConstant.NOTIFICATION_DATA, GsonUtil.getGson().toJson(notification));
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.OTHER, TelemetryPageId.SERVER_NOTIFICATION, TelemetryAction.NOTIFICATION_RECEIVED, null, valuesMap));
        // Server notification
        long triggerAtMillis = DateUtil.parse(notification.getTime(), DateUtil.DATETIME_FORMAT).getMillis();
        long currentTime = DateUtil.getEpochTime();
        if (notification.getValidity() == -1 || currentTime < triggerAtMillis + (notification.getValidity() * _1_MIN)) {
            isSchedule = true;
        }

        LocalBroadcastManager.getInstance(GlobalApplication.getInstance()).sendBroadcast(Util.getRefreshNotificationsIntent());

        AlarmManagerUtil alarmManagerUtil = new AlarmManagerUtil(mContext);

        Intent intent = new Intent(mContext, LocalNotificationService.class);
        intent.setAction("" + Math.random());
        if (isSchedule) {
            Bundle extras = new Bundle();
            extras.putByteArray(Constants.BUNDLE_KEY_NOTIFICATION_DATA_MODEL, SerializableUtil.serialize(notification));
            intent.putExtras(extras);
            alarmManagerUtil.scheduleAlarm(intent, Util.aton(notification.getMsgid()), triggerAtMillis);
        }
    }

    /**
     * Create and show a simple notification
     *
     * @param genieNotification
     */
    public void handleNotificationAction(Notification genieNotification) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_launcher);
//         TODO: get the silhoutte icon for notification   builder.setSmallIcon(R.drawable.ic_ekstep_silhouette);
        } else {
            builder.setSmallIcon(R.drawable.ic_launcher);
        }

        builder.setContentTitle(genieNotification.getTitle());
        builder.setContentText(genieNotification.getMsg());
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(genieNotification.getMsg()));
        builder.setAutoCancel(true);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(defaultSoundUri);

        if (genieNotification.getActionid() != NotificationActionId.DO_NOTHING) {
            builder.setContentIntent(getPendingIntent(genieNotification));
        }

        android.app.Notification notification = builder.build();
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify( Util.aton(genieNotification.getMsgid()), notification);
    }

    private PendingIntent getPendingIntent(Notification genieNotification) {
        // TODO: 11/24/2017 - relook Launcher activity for notification.
        Intent intent = new Intent(mContext, MainActivity.class);

        intent.putExtra(Constants.BUNDLE_KEY_NOTIFICATION_DATA_MODEL, genieNotification);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis() /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

}
