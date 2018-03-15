package org.sunbird.utils;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.ekstep.genieservices.commons.bean.CorrelationData;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunbird.GlobalApplication;
import org.sunbird.models.CurrentGame;
import org.sunbird.telemetry.enums.CoRelationType;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created on 18/08/17.
 *
 * @author anil
 */
public class Util {

    public static String bytesToHuman(long size) {
        long Kb = 1 * 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;
        long Tb = Gb * 1024;
        long Pb = Tb * 1024;
        long Eb = Pb * 1024;

        if (size < Kb) return floatForm(size) + "";
        if (size >= Kb && size < Mb) return floatForm((double) size / Kb) + "";
        if (size >= Mb && size < Gb) return floatForm((double) size / Mb) + "";
        if (size >= Gb && size < Tb) return floatForm((double) size / Gb) + "";
        if (size >= Tb && size < Pb) return floatForm((double) size / Tb) + "";
        if (size >= Pb && size < Eb) return floatForm((double) size / Pb) + "";
        if (size >= Eb) return floatForm((double) size / Eb) + "";

        return "0.00";
    }

    private static String floatForm(double size) {
        return String.format(Locale.US, "%.2f", size);
    }

    /**
     * Get Correlation Context
     *
     * @return
     */
    public static String getCorrelationContext() {
        return GlobalApplication.getPreferenceWrapper().getString(PreferenceKey.METADATA_CORELATION_ID, null);
    }

    /**
     * Set Correlation Id Context
     *
     * @param coRelationContext
     */
    public static void setCorrelationContext(String coRelationContext) {
        if (!StringUtil.isNullOrEmpty(coRelationContext)) {
            GlobalApplication.getPreferenceWrapper().putString(PreferenceKey.METADATA_CORELATION_ID, coRelationContext);
        } else {
            GlobalApplication.getPreferenceWrapper().putString(PreferenceKey.METADATA_CORELATION_ID, null);
        }
    }

    private static String getCorrelationId() {
        return GlobalApplication.getPreferenceWrapper().getString("id_" + getCorrelationContext(), null);
    }

    public static void setCorrelationId(String id) {
        GlobalApplication.getPreferenceWrapper().putString("id_" + getCorrelationContext(), id);
    }

    private static String getCorrelationType() {
        return GlobalApplication.getPreferenceWrapper().getString("type_" + getCorrelationContext(), null);
    }

    public static void setCorrelationType(String id) {
        GlobalApplication.getPreferenceWrapper().putString("type_" + getCorrelationContext(), id);
    }

    /**
     * Returns Correlation data
     *
     * @return
     */
    public static List<CorrelationData> getCorrelationList() {
        List<CorrelationData> cdata = null;
        String coRelationContext = getCorrelationContext();
        String coRelationId;
        if (!StringUtil.isNullOrEmpty(coRelationContext)) {
            coRelationId = getCorrelationId();
            if (!StringUtil.isNullOrEmpty(coRelationId)) {
                String coRelationType = CoRelationType.API + "-" + getCorrelationType();
                CorrelationData correlationData = new CorrelationData(coRelationId, coRelationType);
                cdata = getCdata(correlationData);
            }
        }

        return cdata;
    }

    private static List<CorrelationData> getCdata(CorrelationData... correlationData) {
        List<CorrelationData> cdata = new ArrayList<>();

        for (CorrelationData data : correlationData) {
            cdata.add(data);
        }

        return cdata;
    }

    /**
     * Get current game.
     *
     * @return
     */
    public static List<CurrentGame> getCurrentGameList() {
        List<CurrentGame> currentGameList;

        String jsonContents = GlobalApplication.getPreferenceWrapper().getString(PreferenceKey.CURRENT_GAME, null);
        if (!TextUtils.isEmpty(jsonContents)) {
            CurrentGame[] contentItems = GsonUtil.fromJson(jsonContents, CurrentGame[].class);
            currentGameList = Arrays.asList(contentItems);
            currentGameList = new ArrayList<>(currentGameList);
        } else {
            currentGameList = new ArrayList<>();
        }

        return currentGameList;
    }

    /**
     * Save current Game.
     *
     * @param currentGameList
     */
    public static void saveCurrentGame(List<CurrentGame> currentGameList) {
        String jsonContents = GsonUtil.toJson(currentGameList);
        GlobalApplication.getPreferenceWrapper().putString(PreferenceKey.CURRENT_GAME, jsonContents);
    }


    /**
     * Get the cdata Status.
     *
     * @return
     */
    public static boolean getCdataStatus() {
        return GlobalApplication.getPreferenceWrapper().getBoolean(PreferenceKey.DONT_SEND_CDATA, false);
    }

    /**
     * Set tthe cdata Status.
     *
     * @param status
     */
    public static void setCdataStatus(boolean status) {
        GlobalApplication.getPreferenceWrapper().putBoolean(PreferenceKey.DONT_SEND_CDATA, status);
    }

    public static String postFile(String url, File file, String apiToken, String userAccessToken, String userId, String cb) {
        OkHttpClient client = new OkHttpClient();
        final MediaType MEDIA_TYPE = MediaType.parse("image/jpeg");
        try {
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE, file))
                    .addFormDataPart("container", "user/" + userId)
                    .build();
            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + apiToken)
                    .addHeader("x-authenticated-user-token", userAccessToken)
                    .addHeader("Accept", "application/json ")
                    .addHeader("Content-Type", "application/json ")
                    .url(url)
                    .post(body)
                    .build();
            Log.e("Utils", "request url: " + request.url());
            Response response = client.newCall(request).execute();
            JSONObject resData = new JSONObject();
            resData.put("responseCode", response.code());
            JSONObject resBody = new JSONObject(response.body().string());
            resData.put("responseBody", resBody);
            Log.e("Utils", "postFile: " + resData.toString());
            return resData.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Intent getRefreshNotificationsIntent() {
        return new Intent(Constants.INTENT_ACTION_REFRESH_NOTIFICATION);
    }

    public static int aton(String input) { //alphanumeric to integer
        int out = 0;
        int len = input.length();
        String pools = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";
        for (int i = 0; i < len; i++) {
            char c = input.charAt(len - (i + 1));
            out += (pools.indexOf(c) * ((int) Math.pow(63, i)));
        }
        return out;
    }

    public static String parseUserTokenFromAccessToken(String userAccessToken) {
        String value = userAccessToken.substring(userAccessToken.indexOf("."), userAccessToken.lastIndexOf("."));
        String userToken = null;
        JSONObject jo = null;
        try {
            jo = new JSONObject(decodeBase64(value));
            userAccessToken = jo.get("sub").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return userAccessToken;
    }

    public static String decodeBase64(String data) throws UnsupportedEncodingException {
        byte[] dataText = Base64.decode(data, Base64.DEFAULT);
        String text = new String(dataText, "UTF-8");
        return text;
    }

}