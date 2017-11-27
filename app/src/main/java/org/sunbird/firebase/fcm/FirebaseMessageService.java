package org.sunbird.firebase.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.sunbird.models.Notification;
import org.sunbird.notification.NotificationManagerUtil;
import org.sunbird.notification.enums.NotificationActionId;

import java.util.Map;

/**
 * Created  on 15/11/17.
 *
 * @author JUSPAY\nikith.shetty
 */
public class FirebaseMessageService extends FirebaseMessagingService {

    private static final String TAG = FirebaseMessageService.class.getName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();
            if (data.containsKey("notificationpayload")) {
                String geniepayload = data.get("notificationpayload");

                Notification notification = GsonUtil.fromJson(geniepayload, Notification.class);
                if (notification != null && (notification.getActionid() == NotificationActionId.ANNOUNCEMENT_LIST || notification.getActionid() == NotificationActionId.ANNOUNCEMENT_DETAIL)) {
                    getAnnouncement();
                }
                NotificationManagerUtil notificationManagerUtil = new NotificationManagerUtil(FirebaseMessageService.this);
                notificationManagerUtil.handleNotification(notification);
            }
        }

        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

//    private void sendNotification(Map<String, String> data) {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("intentFrom", "fcmNotification");
//        intent.putExtra("notifData", GsonUtil.toJson(data));
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
////        String channelId = "2364";
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentTitle(data.get("title"))
//                        .setContentText(data.get("summary"))
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//    }

    private void getAnnouncement() {

    }
}
