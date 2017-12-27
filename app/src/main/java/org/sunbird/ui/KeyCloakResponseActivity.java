package org.sunbird.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import org.ekstep.genieservices.commons.bean.enums.InteractionType;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunbird.BuildConfig;
import org.sunbird.R;
import org.sunbird.telemetry.TelemetryAction;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.telemetry.TelemetryPageId;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AmitRohan on 01/08/17.
 */
public class KeyCloakResponseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in = getIntent();
        Uri data = in.getData();
        Log.e("GOT DATA", data.toString());

        String callBackUrl = data.toString();
        int startIndex = callBackUrl.indexOf("=");

        String token = callBackUrl.substring(startIndex + 1, callBackUrl.length());
        Log.e("\n\n\nToken  ", token);

        final String userCode = token;

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString("logged_in", "NO")
                .apply();

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                RequestBody body = RequestBody.create(mediaType, "redirect_uri=https%3A%2F%2F" + getBaseContext().getResources().getString(R.string.api_url) + "%2Foauth2callback&code=" + userCode + "&grant_type=authorization_code&client_id=android");
                Request request = new Request.Builder()
                        .url(BuildConfig.REDIRECT_BASE_URL + "/auth/realms/sunbird/protocol/openid-connect/token")
                        .post(body)
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
//                    Log.e("response from keycloak", GsonUtil.toJson(response));
                    String jwtToken = response.body().string();
                    JSONObject jResponse = new JSONObject(jwtToken);
                    String refreshToken = jResponse.get("refresh_token").toString();
                    jwtToken = jResponse.get("access_token").toString();

                    String value = jwtToken.substring(jwtToken.indexOf("."), jwtToken.lastIndexOf("."));

                    JSONObject jo = new JSONObject(decodeBase64(value));
                    if (jo.get("sub").toString().length() > 0) {

                        PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                                .edit()
                                .putString("logged_in", "YES")
                                .putString("user_token", jo.get("sub").toString())
                                .putString("user_access_token", jwtToken)
                                .putString("refresh_token", refreshToken)
                                .apply();

                        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.OTHER, TelemetryPageId.LOGIN, TelemetryAction.LOGIN_SUCCESS, jo.get("sub").toString(), null));
                    }

                    Intent openMain = new Intent(KeyCloakResponseActivity.this, MainActivity.class);
                    openMain.putExtra("user_id", jo.get("sub").toString());
                    openMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(openMain);
                    finish();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String decodeBase64(String data) throws UnsupportedEncodingException {
        byte[] dataText = Base64.decode(data, Base64.DEFAULT);
        String text = new String(dataText, "UTF-8");
        return text;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}