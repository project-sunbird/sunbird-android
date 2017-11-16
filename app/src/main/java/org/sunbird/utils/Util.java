package org.sunbird.utils;

import android.text.TextUtils;
import android.util.Log;

import org.ekstep.genieservices.commons.bean.CorrelationData;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.json.JSONObject;
import org.sunbird.GlobalApplication;
import org.sunbird.models.CurrentGame;
import org.sunbird.telemetry.enums.CoRelationIdContext;
import org.sunbird.telemetry.enums.CoRelationType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by vinay.narayana on 18/08/17.
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
     * Get Corelation Context
     *
     * @return
     */
    public static String getCoRelationIdContext() {
        return GlobalApplication.getPreferenceWrapper().getString(PreferenceKey.METADATA_CORELATION_ID, null);
    }

    /**
     * Set Corelation Id Context
     *
     * @param coRelationIdContext
     */
    public static void setCoRelationIdContext(String coRelationIdContext) {
        if (!StringUtil.isNullOrEmpty(coRelationIdContext)) {
            GlobalApplication.getPreferenceWrapper().putString(PreferenceKey.METADATA_CORELATION_ID, coRelationIdContext);
        } else {
            GlobalApplication.getPreferenceWrapper().putString(PreferenceKey.METADATA_CORELATION_ID, null);
        }
    }

    public static String getCoRelationType() {
        return GlobalApplication.getPreferenceWrapper().getString("type_" + getCoRelationIdContext(), null);
    }

    public static void setCoRelationType(String id) {
        GlobalApplication.getPreferenceWrapper().putString("type_" + getCoRelationIdContext(), id);
    }

    /**
     * Get Corelation Context
     *
     * @return
     */
    public static String getCoRelationId() {
        return GlobalApplication.getPreferenceWrapper().getString(getCoRelationIdContext(), null);
    }

    /**
     * Returns CoRelationId
     *
     * @return
     */
    public static List<CorrelationData> getCoRelationList() {
        List<CorrelationData> cdata = null;
        String coRelationContext = getCoRelationIdContext();
        String coRelationId = null;
        if (!StringUtil.isNullOrEmpty(coRelationContext)) {
            coRelationId = getCoRelationId();
            if (!StringUtil.isNullOrEmpty(coRelationId)) {
                String coRelationType = CoRelationType.API + "-" + getCoRelationType();
                CorrelationData corelationData = new CorrelationData(coRelationId, coRelationType);
                cdata = getCdata(corelationData);
            }
        }

        return cdata;
    }

    public static List<CorrelationData> getCdata(CorrelationData... correlationData) {
        List<CorrelationData> cdata = new ArrayList<>();

        for (CorrelationData data : correlationData) {
            cdata.add(data);
        }

        return cdata;
    }

    /**
     * Get cuurent game.
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

    /**
     * Set response message id value.
     *
     * @param responseMessageId
     */
    public static void setCourseandResourceSearchApiResponseMessageId(String responseMessageId) {
        GlobalApplication.getPreferenceWrapper().putString(CoRelationIdContext.COURSE_AND_RESOURCE_SEARCH, responseMessageId);
    }

    public static void setCourseSearchApiResponseMessageId(String responseMessageId) {
        GlobalApplication.getPreferenceWrapper().putString(CoRelationIdContext.COURSE_SEARCH, responseMessageId);
    }

    public static void setResourceSearchApiResponseMessageId(String responseMessageId) {
        GlobalApplication.getPreferenceWrapper().putString(CoRelationIdContext.RESOURCE_SEARCH, responseMessageId);
    }

    /**
     * Set response message id value.
     *
     * @param responseMessageId
     */
    public static void setHomePageAssembleApiResponseMessageId(String responseMessageId) {
        GlobalApplication.getPreferenceWrapper().putString(CoRelationIdContext.HOME_PAGE, responseMessageId);
    }

    public static void setCoursePageAssembleApiResponseMessageId(String responseMessageId) {
        GlobalApplication.getPreferenceWrapper().putString(CoRelationIdContext.COURSE_PAGE, responseMessageId);
    }

    public static void setResourcePageAssembleApiResponseMessageId(String responseMessageId) {
        GlobalApplication.getPreferenceWrapper().putString(CoRelationIdContext.RESOURCE_PAGE, responseMessageId);
    }

    public static String getCurrentLocalDateTimeStamp() {
        String time ="";
        try {
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date());
            TimeZone tz = TimeZone.getDefault();
            String timeZone = tz.getDisplayName(false, TimeZone.SHORT);
            time += timeZone.substring(3, 6) + timeZone.substring(7,9);
        } catch (Exception e) {
            Log.e("Error", "getCurrentLocalDateTimeStamp erroe: "+e );
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date());
            time+="+0530";
        }
        return time;
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
                    .addHeader("X-Consumer-ID", "33a6ddbe-0c83-4d26-a08d-29138c898825 ")
                    .addHeader("X-Device-ID", "X-Device-ID ")
                    .addHeader("X-msgid", "8e27cbf5-e299-43b0-bca7-8347f7e5abcf ")
                    .addHeader("ts", getCurrentLocalDateTimeStamp())
                    .addHeader("Accept", "application/json ")
                    .addHeader("X-Source", "web ")
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
}