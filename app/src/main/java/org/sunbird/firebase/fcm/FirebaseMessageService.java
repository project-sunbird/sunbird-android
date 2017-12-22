package org.sunbird.firebase.fcm;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.ekstep.genieservices.commons.utils.Base64Util;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.json.JSONArray;
import org.json.JSONException;
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
                    getAnnouncement(notification.getActiondata().getAnnouncementId());
                }
                NotificationManagerUtil notificationManagerUtil = new NotificationManagerUtil(FirebaseMessageService.this);
                notificationManagerUtil.handleNotification(notification);
            }
        }
    }

    private void getAnnouncement(final String announcementId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //make api call to get announcement details
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String userId = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("user_token", "__failed");
                String userAccessToken = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("user_access_token", "__failed");
                String apiToken = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("api_token", "__failed");
                String url = BuildConfig.REDIRECT_BASE_URL + "/api/announcement/v1/get/" + announcementId;
                try {
                    Request request = new Request.Builder()
                            .addHeader("Authorization", "Bearer " + apiToken)
                            .addHeader("x-authenticated-user-token", userAccessToken)
                            .addHeader("Accept", "application/json ")
                            .addHeader("Content-Type", "application/json ")
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.code() == 200){
                        JSONObject resBody = new JSONObject(response.body().string());
                        JSONObject result = new JSONObject(resBody.get("result").toString());
                        JSONObject announcement = new JSONObject(result.get("announcement").toString());
                        appendToSavedList(announcement);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "announcement get api exception" + e);
                }
            }
        }).start();
    }

    private void appendToSavedList(JSONObject announcement) {
        String ret = SQLBlobStore.getData(getBaseContext(), "savedAnnouncements");
        JSONObject announcementJSON;
        try {
            if (ret == null || ret == "undefined") {
                announcementJSON = new JSONObject();
                announcementJSON.put("count", 1);
                announcementJSON.put("announcements", new JSONArray(announcement));
            } else {
                announcementJSON = new JSONObject(new String(Base64Util.decode(ret, Base64Util.DEFAULT)));
                announcementJSON.put("count", announcementJSON.getInt("count") + 1);
                JSONArray announcementList = announcementJSON.getJSONArray("announcements");
                announcementList.put(announcement);
            }
            SQLBlobStore.setData(getBaseContext(), "savedAnnouncements", Base64Util.encodeToString(announcementJSON.toString().getBytes(), Base64Util.DEFAULT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
