package org.sunbird.firebase.fcm;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.json.JSONObject;
import org.sunbird.BuildConfig;
import org.sunbird.models.Notification;
import org.sunbird.notification.NotificationManagerUtil;
import org.sunbird.notification.enums.NotificationActionId;
import org.sunbird.utils.SQLBlobStore;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    }

    private void getAnnouncement() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //make api call to get announcement details
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String userId = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("user_token", "__failed");
                String userAccessToken = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("user_access_token", "__failed");
                String apiToken = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("api_token", "__failed");
                String url = BuildConfig.REDIRECT_BASE_URL + "/api/announcement/v1/user/inbox";
                try {
                    String jsonBody = "{\"request\": {" +
                            "\"userId\": \"" + userId + "\"" +
                            "}}";
                    RequestBody body = RequestBody.create(JSON, jsonBody);
                    Request request = new Request.Builder()
                            .addHeader("Authorization", "Bearer " + apiToken)
                            .addHeader("x-authenticated-user-token", userAccessToken)
                            .addHeader("Accept", "application/json ")
                            .addHeader("Content-Type", "application/json ")
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    JSONObject resData = new JSONObject();
                    Log.d(TAG, "announcement list " + resData.toString());
                    if (response.code() == 200){
                        JSONObject resBody = new JSONObject(response.body().string());
                        JSONObject result = new JSONObject(resBody.get("result").toString());
                        int count = result.getInt("count");
                        if (count > 0) {
                            //save to db
                            SQLBlobStore.setData(getBaseContext(), "savedAnnouncements", result.toString());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "announcement get api exception");
                }
            }
        }).start();
    }
}
