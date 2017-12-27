package org.sunbird.core;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.cordova.LOG;
import org.ekstep.genieservices.commons.bean.enums.InteractionType;
import org.ekstep.genieservices.commons.utils.Base64Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunbird.BuildConfig;
import org.sunbird.GlobalApplication;
import org.sunbird.R;
import org.sunbird.models.ApiResponse;
import org.sunbird.telemetry.TelemetryAction;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryConstant;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.telemetry.TelemetryPageId;
import org.sunbird.telemetry.enums.CoRelationIdContext;
import org.sunbird.telemetry.enums.EntityType;
import org.sunbird.ui.ListViewAdapter;
import org.sunbird.ui.MainActivity;
import org.sunbird.ui.MyRecyclerViewAdapter;
import org.sunbird.ui.TabLayout;
import org.sunbird.ui.ViewPagerAdapter;
import org.sunbird.utils.Constants;
import org.sunbird.utils.FileDownloader;
import org.sunbird.utils.FileDownloader.OnFileDownloadProgressChangedListener;
import org.sunbird.utils.FileUtil;
import org.sunbird.utils.GenieWrapper;
import org.sunbird.utils.ImagePicker;
import org.sunbird.utils.KeyValueStore;
import org.sunbird.utils.SQLBlobStore;
import org.sunbird.utils.Util;
import org.sunbird.utils.WebSocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import in.juspay.mystique.DynamicUI;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static java.lang.Integer.parseInt;

/**
 * Created by stpl on 24/2/17.
 */
public class JsInterface {

    //private KeyValueStore keyValueStore;
    private final static String LOG_TAG = JsInterface.class.getName();
    private static final int SEND_SMS_REQUEST = 8;
    //TODO : KEYCLOACK REDIRECT URL, change in manifest for deep linking
    private static String REDIRECT_URI = BuildConfig.REDIRECT_BASE_URL + "/oauth2callback";
    private final int SMS_PERMISSION_CODE = 1, PHONE_STATE_PERMISSION_CODE = 2, STORAGE_PERMISSION_CODE = 3, COARSE_LOCATION_CODE = 4, CAMERA_PERMISSION_CODE = 5;
    private ListViewAdapter listViewAdapter = null;
    private MyRecyclerViewAdapter recylerViewAdapter = null;
    private String downloadCallback = "";
    private Context context;
    private MainActivity activity;
    private DynamicUI dynamicUI;
    /**
     * Get the download progress callbacks here
     */
    private OnFileDownloadProgressChangedListener mChangedListener = new OnFileDownloadProgressChangedListener() {

        @Override
        public void onProgressChanged(float currentProgress) {
            String javascript = String.format("window.callJSCallback('%s','%s');", downloadCallback, String.valueOf(currentProgress));
            dynamicUI.addJsToWebView(javascript);
        }

        @Override
        public void onFileDownloaded(String currentPath) {
            String javascript = String.format("window.callJSCallback('%s','%s');", downloadCallback, "finished");
            dynamicUI.addJsToWebView(javascript);
            Log.e("download!", "onFileDownloaded: finished");
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(currentPath)));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
        }

        @Override
        public void onFailure() {
            String javascript = String.format("window.callJSCallback('%s','%s');", downloadCallback, "failure");
            dynamicUI.addJsToWebView(javascript);
        }

        @Override
        public void onDownloadStart() {
            String javascript = String.format("window.callJSCallback('%s','%s');", downloadCallback, "start");
            dynamicUI.addJsToWebView(javascript);
        }
    };
    private WebSocket ws;
    private int RESULT_LOAD_IMG = 88;
    private int REQUEST_CODE_PERMISSION = 0;
    private String permissionCallback;
    private DatePickerDialog datePicker;
    private KeyValueStore keyValueStore;
    private GenieWrapper genieWrapper;
    private Executor pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private FileDownloader mFileDownloader;
    private DownloadFileAsync[] mDownloadFileAsyncArray = new DownloadFileAsync[100];

    public JsInterface(MainActivity activity, DynamicUI dynamicUI, WebSocket ws) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.dynamicUI = dynamicUI;
        this.ws = ws;
        keyValueStore = new KeyValueStore(context);
        genieWrapper = new GenieWrapper(activity, dynamicUI);
    }

    private ArrayList jsonToArrayList(JSONArray jsonArray, String key, String type) throws JSONException {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject element = jsonArray.getJSONObject(i);
            if (type.equalsIgnoreCase("Int")) {
                arrayList.add(Integer.parseInt(element.getString(key)));
            } else {
                arrayList.add(element.getString(key));
            }
        }
        return arrayList;
    }

    @JavascriptInterface
    public void keyCloakLogin(final String OAUTH_URL, final String CLIENT_ID) {

        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.LOGIN, TelemetryAction.LOGIN_INITIATE, null, null));

        CustomTabsIntent.Builder mBuilder = new CustomTabsIntent.Builder(getSession());
        CustomTabsIntent mIntent = mBuilder.build();

        String keyCloackAuthUrl = OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&scope=offline_access&client_id=" + CLIENT_ID;
        Log.e("URL HITTING:", keyCloackAuthUrl);
        mIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mIntent.launchUrl(activity, Uri.parse(keyCloackAuthUrl));

        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(TelemetryPageId.LOGIN));
    }

    @JavascriptInterface
    public void keyCloakLogout(final String OLOGOUT_URL) {
        CustomTabsIntent.Builder mBuilder = new CustomTabsIntent.Builder(getSession());
        mBuilder.setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        CustomTabsIntent mIntent = mBuilder.build();
        String keyCloackAuthUrl = OLOGOUT_URL + "?redirect_uri=" + REDIRECT_URI;
        Log.e("URL HITTING:", keyCloackAuthUrl);
        mIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.launchUrl(activity, Uri.parse(keyCloackAuthUrl));
    }

    private CustomTabsSession getSession() {
        CustomTabsClient mClient = ((MainActivity) activity).getCustomTabsClient();
        return mClient.newSession(new CustomTabsCallback() {
            @Override
            public void onNavigationEvent(int navigationEvent, Bundle extras) {

                switch (navigationEvent) {
                    case NAVIGATION_STARTED:
                        // Sent when the tab has started loading a page.
                        break;
                    case NAVIGATION_FINISHED:
                        // Sent when the tab has finished loading a page.

                        break;
                    case NAVIGATION_FAILED:
                        // Sent when the tab couldn't finish loading due to a failure.
                        break;
                    case NAVIGATION_ABORTED:
                        // Sent when loading was aborted by a user action before it finishes like clicking on a link
                        // or refreshing the page.
                        break;
                }
            }

        });
    }

    @JavascriptInterface
    public void listViewAdapter(final String id, String text, int itemCount) throws Exception {
        int listViewId = parseInt(id);
        final ListView listView = (ListView) activity.findViewById(listViewId);
        JSONArray jsonArray = new JSONArray(text);
        ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
        ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");
        ArrayList<Integer> viewTypeArrayList = jsonToArrayList(jsonArray, "viewType", "Int");
        listViewAdapter = new ListViewAdapter(context, itemCount, valueArrayList, viewJSXArrayList, viewTypeArrayList, dynamicUI);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    listView.setAdapter(listViewAdapter);
                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                }
            }
        });
    }

    @JavascriptInterface
    public void setUpCollapsingToolbar(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    CollapsingToolbarLayout cLayout = (CollapsingToolbarLayout) activity.findViewById(parseInt(id));
                    cLayout.setFitsSystemWindows(true);

                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                }
            }
        });
    }

    @JavascriptInterface
    public void setGradient(final String id, final String fromColor, final String toColor) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int resid = Integer.parseInt(id);
                    View layout = activity.findViewById(resid);
                    GradientDrawable gd = new GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[]{Color.parseColor(fromColor), Color.parseColor(toColor)});
                    gd.setCornerRadius(0f);

                    if (layout != null)
                        layout.setBackgroundDrawable(gd);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getUrlEncoded(String q) throws UnsupportedEncodingException {
        return URLEncoder.encode(q, "UTF-8");
    }

    @JavascriptInterface
    public void patchApi(final String urlToHit, final String bodyContent, final String userid, final String api_key) throws IOException {
        Log.e("in patch", "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, bodyContent);
                Request request = new Request.Builder()
                        .url(urlToHit)
                        .patch(body)
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("X-Authenticated-User-Token", userid)
                        .addHeader("Authorization", "Bearer " + api_key)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    Log.e("pres!", String.valueOf(response));
                    String callback = String.format("window.__patchCallback('%s');", response.body().string());
                    dynamicUI.addJsToWebView(callback);
                } catch (Exception e) {
                    Log.e("err patch", e.getMessage());
                }
            }
        }).start();
    }

    @JavascriptInterface
    public String getResourceByName(String resName) {
        return getResourceById(activity.getResources().getIdentifier(resName, "string", activity.getPackageName()));
    }

    @JavascriptInterface
    public String getResourceById(int resId) {
        return activity.getResources().getString(resId);
    }

    @JavascriptInterface
    public void recyclerViewAdapter(final String id, final String text, final int itemCount, final int spanCount, final int direction) throws Exception {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int recylerViewId = parseInt(id);
                final RecyclerView recyclerView = (RecyclerView) activity.findViewById(recylerViewId);
                JSONArray jsonArray = null;
                if (recyclerView != null) {
                    try {
                        jsonArray = new JSONArray(text);
                        ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
                        ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");
                        ArrayList<Integer> viewTypeArrayList = jsonToArrayList(jsonArray, "viewType", "Int");
                        Log.e("GOT UI 1", viewJSXArrayList.toString());
                        recylerViewAdapter = new MyRecyclerViewAdapter(context, itemCount, valueArrayList, viewJSXArrayList, viewTypeArrayList, dynamicUI, activity);
                        Log.e("SETTING LAYOUT", "22222");
                        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, direction));
                        Log.e("SETTING ADAPT", "3333");
                        recyclerView.setAdapter(recylerViewAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("NULL", "RECYLER VIEW");
                }
            }
        });
    }

    @JavascriptInterface
    public void setClickFeedback(final String res) {
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Integer resInt = Integer.valueOf(res);
                    View view = activity.findViewById(resInt);
                    TypedValue outValue = new TypedValue();
                    activity.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
                    //noinspection deprecation
                    if (view != null) {
                        view.setBackgroundResource(outValue.resourceId);
                    } else {
                        Log.e(LOG_TAG, "Unable to find view with resID - " + res + " : " + resInt);
                    }
                } catch (Exception e) {
                    //No op
                }
            }
        });
    }

    @JavascriptInterface
    public void setRating(String id, final String rating) {
        final int resId = parseInt(id);
        Log.d("RATING TEXT", rating);
        final float totalRating = Float.parseFloat(rating);
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    RatingBar newBar = new RatingBar(activity, null, R.attr.ratingBarStyleSmall);
                    newBar.setRating(totalRating);
                    newBar.setScaleX(0.95f);
                    newBar.setScaleY(0.95f);

                    Drawable progress = newBar.getProgressDrawable();
                    DrawableCompat.setTint(progress, Color.parseColor("#007AFF"));

                    RatingBar ratingBar = (RatingBar) activity.findViewById(resId);
                    ViewGroup parent = (ViewGroup) ratingBar.getParent();
                    int index = parent.indexOfChild(ratingBar);
                    parent.removeViewAt(index);
                    parent.addView(newBar, index);
                } catch (Exception e) {
                    //No op
                }
            }
        });
    }

    @JavascriptInterface
    public void handleImeAction(String id, final String callback) {
        final int resId = parseInt(id);
        Log.d("HANDLE IME ACITON", "" + id);
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    final EditText editText = (EditText) activity.findViewById(resId);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

                    editText.setOnEditorActionListener(
                            new EditText.OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                                        String javascript = String.format("window.callJSCallback('%s','%s');", callback, editText.getText().toString());
                                        dynamicUI.addJsToWebView(javascript);

                                        return true;
                                    }
                                    return false;
                                }
                            });
                } catch (Exception e) {
                    //No op
                    e.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @JavascriptInterface
    public void getContentImportStatus(final String identifier, final String callback) {
        genieWrapper.getImportStatus(identifier, callback);
    }

    @android.webkit.JavascriptInterface
    public void showCalender(final String callback, final String minDate, final String maxDate, final String currentSelected) {
        Log.e("\n\nCALAN :", "showCAlender");
        this.activity.runOnUiThread(new Runnable() {
            public void run() {
                final Calendar myCalendar = Calendar.getInstance();

                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String date = String.format("window.callJSCallback('%s','%s');", callback, year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);
                        dynamicUI.addJsToWebView(date);
                    }
                };

                if (currentSelected != null && !currentSelected.isEmpty() && !currentSelected.equals("undefined")) {
                    myCalendar.setTimeInMillis(dateToMillisecond(currentSelected));
                }
                datePicker = new DatePickerDialog(activity, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                if (minDate != null && !minDate.isEmpty() && !minDate.equals("undefined")) {

                    datePicker.getDatePicker().setMinDate(dateToMillisecond(minDate));
                }
                if (maxDate != null && !maxDate.isEmpty() && !maxDate.equals("undefined")) {
                    datePicker.getDatePicker().setMaxDate(dateToMillisecond(maxDate));
                } else {
                    datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                }
                datePicker.show();
            }
        });
    }

    private long dateToMillisecond(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate;
        try {
            newDate = sdf.parse(date);
            return newDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @JavascriptInterface
    public void viewPagerAdapter(final String id, final String tabId, String viewValues, String tabValue, final String callback) throws Exception {
        try {
            int viewPagerId = parseInt(id);
            int tabLayoutId = parseInt(tabId);
            Log.d("viewpageer adapter", id);
            final ViewPager viewPager = (ViewPager) activity.findViewById(viewPagerId);
            JSONArray jsonArray = new JSONArray(viewValues);
            JSONArray jsonTitleArray = new JSONArray(tabValue);
            final ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
            ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");
            ArrayList<String> titleArrayList = jsonToArrayList(jsonTitleArray, "value", "String");
            final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(context, valueArrayList, viewJSXArrayList, titleArrayList, dynamicUI);
            final TabLayout tabLayout = (TabLayout) activity.findViewById(tabLayoutId);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewPager.setAdapter(viewPagerAdapter);
                    tabLayout.setupWithViewPager(viewPager);
                    viewPager.setOffscreenPageLimit(viewJSXArrayList.size());
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            Log.d("CURRENT ITEM", viewPager.getCurrentItem() + "");
                            String javascript = String.format("window.callJSCallback('%s','%s');", callback, position);
                            dynamicUI.addJsToWebView(javascript);
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PRINT :", "ERROR");
        }
    }

    @JavascriptInterface
    public void getContentDetails(String content_id, String callback) {
        Log.e("GEnie", "---------------------------------------------");
        genieWrapper.getContentDetails(callback, content_id);
    }

    @JavascriptInterface
    public void getAllLocalContent(String callback) {
        genieWrapper.getAllLocalContent(callback);
    }

    @JavascriptInterface
    public void deleteContent(String content_id, String callback) {
        genieWrapper.deleteContent(content_id, callback);
    }

    @JavascriptInterface
    public void setInputType(String viewId, String inputType) {
        View layout = activity.findViewById(Integer.parseInt(viewId));
        EditText editText = (EditText) layout;

        if ("password".equals(inputType)) {
            editText.setTransformationMethod(new PasswordTransformationMethod());
        }
    }

    @JavascriptInterface
    public void searchContent(String callback, String filterParams, String query, String type, String status, int count) {
        Log.e("ser!", query);
        genieWrapper.searchContent(callback, filterParams, query, type, status, count);
//        genieWrapper.filterSearch(callback,query);
    }

    @JavascriptInterface
    public void importCourse(String course_id, String isChild) {
        Log.i("import course", "");
        genieWrapper.importCourse(course_id, isChild);
    }

    @JavascriptInterface
    public void playContent(String contentDetails) {
        try {
            genieWrapper.playContent(contentDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void getLocalContentStatus(String contentId, String callback) {
        Log.e("in Js interface", "local status");
        genieWrapper.getLocalContentStatus(contentId, callback);
    }

    @JavascriptInterface
    public void getContentType(String contentId, String callback) {
        Log.e("in js content type", "");
        genieWrapper.getContentType(contentId, callback);
    }

    @JavascriptInterface
    public void getChildContent(String contentId, String callback) {
        genieWrapper.getCourseContent(callback, contentId);
    }

    @JavascriptInterface
    public void getApiToken(String callback) {
        genieWrapper.getMobileDeviceBearerToken(callback);
    }

    @JavascriptInterface
    public void endContent() {
        genieWrapper.endContent();
    }

    @JavascriptInterface
    public void syncTelemetry() {
        genieWrapper.syncTelemetry();
    }

    @JavascriptInterface
    public void cancelDownload(String contentId) {
        genieWrapper.cancelDownload(contentId);
    }

    @JavascriptInterface
    public void patchRequest(String url, String data, String headers) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
    }

    @JavascriptInterface
    public void logSignUpInitiation() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.SIGNUP, TelemetryAction.SIGNUP_INITIATE, null, null));
    }

    @JavascriptInterface
    public void logSignUpSuccess() {
//        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.OTHER, TelemetryPageId.SIGNUP, TelemetryAction.SIGNUP_SUCCESS));
    }

    @JavascriptInterface
    public void logLogoutInitiate(String user_token) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.LOGOUT, TelemetryAction.LOGOUT_INITIATE, user_token, null));
    }

    @JavascriptInterface
    public void logLogoutSuccess(String user_token) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.OTHER, TelemetryPageId.LOGOUT, TelemetryAction.LOGOUT_SUCCESS, user_token, null));
    }

    @JavascriptInterface
    public void logResourceDetailScreenEvent(String identifier) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.SHOW, TelemetryPageId.RESOURCE_HOME, null, identifier, null));
    }

    @JavascriptInterface
    public void logCourseDetailScreenEvent(String identifier) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.SHOW, TelemetryPageId.COURSE_HOME, null, identifier, null));
    }

    @JavascriptInterface
    public void logContentDetailScreenEvent(String identifier) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.SHOW, TelemetryPageId.CONTENT_DETAIL, null, identifier, null));
    }

    @JavascriptInterface
    public void logCorrelationPageEvent(String type, String id, String ctype) {
        switch (type) {
            case "HOME":
                Util.setCoRelationType(ctype);
                Util.setCoRelationIdContext(CoRelationIdContext.HOME_PAGE);
                Util.setHomePageAssembleApiResponseMessageId(id);
                break;
            case "COURSES":
                Util.setCoRelationType(ctype);
                Util.setCoRelationType(CoRelationIdContext.COURSE_PAGE);
                Util.setCoursePageAssembleApiResponseMessageId(id);
                break;
            case "RESOURCES":
                Util.setCoRelationType(ctype);
                Util.setCoRelationType(CoRelationIdContext.RESOURCE_PAGE);
                Util.setResourcePageAssembleApiResponseMessageId(id);
                break;
            case "SPLASHSCREEN":
                Util.setCoRelationType(CoRelationIdContext.NONE);
                break;
        }
    }

    @JavascriptInterface
    public void logListViewScreenEvent(String type, int count, String criteria) {
        String stageId = "";
        switch (type) {
            case "HOME":
                stageId = TelemetryPageId.COURSE_AND_RESOURSE_LIST;
                break;
            case "COURSES":
                stageId = TelemetryPageId.COURSE_LIST;
                break;
            case "RESOURCES":
                stageId = TelemetryPageId.RESOURCE_LIST;
                break;
        }
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.SEARCH_RESULTS, count);
        eksMap.put(TelemetryConstant.SEARCH_CRITERIA, criteria);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteractWithCoRelation(InteractionType.SHOW, stageId, EntityType.SEARCH_PHRASE, "", eksMap, Util.getCoRelationList()));
    }

    @JavascriptInterface
    public void logContentClickEvent(String type, int position, String phrase, String contentId) {
        String stageId = "";
        switch (type) {
            case "HOME":
                stageId = TelemetryPageId.COURSE_AND_RESOURSE_LIST;
                break;
            case "COURSES":
                stageId = TelemetryPageId.COURSE_LIST;
                break;
            case "RESOURCES":
                stageId = TelemetryPageId.RESOURCE_LIST;
                break;
        }
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.POSITION_CLICKED, position);
        eksMap.put(TelemetryConstant.SEARCH_PHRASE, phrase);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteractWithCoRelation(InteractionType.TOUCH, stageId, TelemetryAction.CONTENT_CLICKED, contentId, eksMap, Util.getCoRelationList()));
    }

    @JavascriptInterface
    public void logCardClickEvent(String type, int position, String sectionName, String contentId) {
        String stageId = "";
        switch (type) {
            case "HOME":
                stageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                stageId = TelemetryPageId.COURSES;
                break;
            case "RESOURCES":
                stageId = TelemetryPageId.RESOURCES;
                break;
        }
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.POSITION_CLICKED, position);
        eksMap.put(TelemetryConstant.SEARCH_PHRASE, sectionName);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteractWithCoRelation(InteractionType.TOUCH, stageId, TelemetryAction.CONTENT_CLICKED, contentId, eksMap, Util.getCoRelationList()));
    }

    @JavascriptInterface
    public void logViewAllClickEvent(String type, String sectionName) {
        String stageId = "", subType = "";
        switch (type) {
            case "HOME":
                stageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                stageId = TelemetryPageId.COURSES;
                break;
            case "RESOURCES":
                stageId = TelemetryPageId.RESOURCES;
                break;

        }
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.SECTION_NAME, sectionName);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, type, TelemetryAction.VIEWALL_CLICKED, null, eksMap));
    }

    @JavascriptInterface
    public void logTabClickEvent(String type) {
        String stageId = "", subType = "";
        switch (type) {
            case "HOME":
                stageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                stageId = TelemetryPageId.COURSES;
                break;
            case "RESOURCES":
                stageId = TelemetryPageId.RESOURCES;
                break;
            case "GROUPS":
                stageId = TelemetryPageId.GROUPS;
                break;
            case "PROFILE":
                stageId = TelemetryPageId.PROFILE;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, stageId, TelemetryAction.TAB_CLICKED, null, null));
    }

    @JavascriptInterface
    public void logTabScreenEvent(String type) {
        String stageId = "";
        switch (type) {
            case "HOME":
                stageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                stageId = TelemetryPageId.COURSES;
                break;
            case "RESOURCES":
                stageId = TelemetryPageId.RESOURCES;
                break;
            case "GROUPS":
                stageId = TelemetryPageId.GROUPS;
                break;
            case "PROFILE":
                stageId = TelemetryPageId.PROFILE;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(stageId));
    }

    @JavascriptInterface
    public void logShareClickEvent(String type) {
        String subType = "";
        if (type.equals("LINK")) {
//            subType = TelemetryAction.SHARE_LINK;
        } else {
//            subType = TelemetryAction.SHARE_FILE;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.SHARE, subType, null, null));
    }

    @JavascriptInterface
    public void logShareContentSuccessEvent(String type, String identifier) {
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.COURSE_HOME, TelemetryAction.SHARE_COURSE_SUCCESS, identifier, null));
        } else if (type.equals("RESOURCES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.RESOURCE_HOME, TelemetryAction.SHARE_RESOURCE_SUCCESS, identifier, null));
        }
    }

    @JavascriptInterface
    public void logShareContentInitiateEvent(String type, String identifier) {
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.COURSE_HOME, TelemetryAction.SHARE_COURSE_INITIATED, identifier, null));
        } else if (type.equals("RESOURCES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.RESOURCE_HOME, TelemetryAction.SHARE_RESOURCE_INITIATED, identifier, null));
        }
    }

    @JavascriptInterface
    public void logshareScreenEvent() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(TelemetryPageId.SHARE));
    }

    @JavascriptInterface
    public void logsplashScreenEvent() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(TelemetryPageId.SPLASH_SCREEN));
    }

    @JavascriptInterface
    public void logFlagClickInitiateEvent(String type, String reason, String comment, String contentId) {
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.REASON, reason);
        eksMap.put(TelemetryConstant.COMMENT, comment);
        String stageId = "";
        if (type.equals("COURSES")) {
            stageId = TelemetryPageId.COURSE_HOME;
        } else if (type.equals("RESOURCES")) {
            stageId = TelemetryPageId.RESOURCE_HOME;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, stageId, TelemetryAction.FLAG_INITIATE, contentId, eksMap));
    }

    @JavascriptInterface
    public void logFlagScreenEvent(String type) {
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(TelemetryPageId.COURSE_HOME_FLAG));
        } else if (type.equals("RESOURCES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(TelemetryPageId.RESOURCE_HOME_FLAG));
        }
    }

    @JavascriptInterface
    public void logFlagClickEvent(String identifier, String type) {
        String stageId = "";
        if (type.equals("COURSES")) {
            stageId = TelemetryPageId.COURSE_HOME_FLAG;
        } else if (type.equals("RESOURCES")) {
            stageId = TelemetryPageId.RESOURCE_HOME_FLAG;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.OTHER, stageId, TelemetryAction.FLAG_SUCCESS, identifier, null));
    }

    @JavascriptInterface
    public void logPreviewScreenEvent() {
//        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(TelemetryPageId.PREVIEW_SCREEN));
    }

    @JavascriptInterface
    public void logPreviewLoginClickEvent() {
//        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.PREVIEW_SCREEN, TelemetryAction.PREVIEW_LOGIN));
    }

    @JavascriptInterface
    public void logPageFilterScreenEvent(String type) {
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(TelemetryPageId.COURSE_PAGE_FILTER));
        } else if (type.equals("RESOURCES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(TelemetryPageId.RESOURCE_PAGE_FILTER));
        }
    }

    @JavascriptInterface
    public void logPageFilterClickEvent(String type) {
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.COURSE_PAGE_FILTER, TelemetryAction.CANCEL, null, null));
        } else if (type.equals("RESOURCES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.RESOURCE_PAGE_FILTER, TelemetryAction.CANCEL, null, null));
        }
    }

    @JavascriptInterface
    public void logAnnouncementListShow() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.SHOW, TelemetryPageId.ANNOUNCEMENT_LIST, null, null, null));
    }

    @JavascriptInterface
    public void logAnnouncementClicked(String from, String announcementId, String pos) {
        String stageid = TelemetryPageId.ANNOUNCEMENT_LIST;
        if (from.equals("HOME")) stageid = TelemetryPageId.HOME;
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.POSISTION, pos);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, stageid, TelemetryAction.ANNOUNCEMENT_CLICKED, announcementId, eksMap));
    }

    @JavascriptInterface
    public void logAnnouncementDeatilScreen(String announcementId) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.SHOW, TelemetryPageId.ANNOUNCEMENT_DETAIL, null, announcementId, null));
    }

    @JavascriptInterface
    public void stopEventBus() {
        Log.e("stop event bus", "");
        genieWrapper.stopEventBus();
    }

    @JavascriptInterface
    public void exportEcar(String contentId, String callback) {
        Log.d("CONTENT ID", contentId);
        genieWrapper.exportEcar(contentId, callback);
    }

    @JavascriptInterface
    public void importEcar(String filePath) {
        genieWrapper.importEcarFile(filePath);
    }

    @JavascriptInterface
    public void setProfile(String uid) {
        genieWrapper.getAllUserProfiles(uid);
    }

    @JavascriptInterface
    public void setAnonymousProfile() {
        genieWrapper.setAnonymousProfile();
    }

    @JavascriptInterface
    public void replaceViewPagerItem(final int position, final String viewValues) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray(viewValues);
                    ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
                    ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");

                    viewPagerAdapter.replacePage(position, valueArrayList.get(0), viewJSXArrayList.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("EXCEPTION :", "GOT ERROR IN REPLACE ITEM VIEWPAGER");
                }
            }
        });
    }

    @JavascriptInterface
    public void changeFontStylePassword(final String id) {
        try {
            final int layoutId = parseInt(id);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EditText editText = (EditText) activity.findViewById(layoutId);
                    if (editText != null)
                        editText.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto/Regular.ttf"));
                    Log.d("INSIDE TRY CATCH", "inside try catch");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public String getApiUrl() {
        String apiUrl = activity.getResources().getString(R.string.api_url);
        return apiUrl;
    }

    @JavascriptInterface
    public void viewPagerAdapter(final String id, String viewValues, String tabValue, final String callback) throws Exception {
        try {
            int viewPagerId = parseInt(id);
            viewPager = (ViewPager) activity.findViewById(viewPagerId);
            Log.d("viewpagerid", viewPagerId + "");
            JSONArray jsonArray = new JSONArray(viewValues);
            JSONArray jsonTitleArray = new JSONArray(tabValue);
            ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
            ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");
            ArrayList<String> titleArrayList = jsonToArrayList(jsonTitleArray, "value", "String");
            viewPagerAdapter = new ViewPagerAdapter(context, valueArrayList, viewJSXArrayList, titleArrayList, dynamicUI);
            if (viewPager != null)
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                            }

                            @Override
                            public void onPageSelected(int position) {
                                Log.d("CURRENT ITEM", viewPager.getCurrentItem() + "");
                                String javascript = String.format("window.callJSCallback('%s','%s');", callback, position);
                                dynamicUI.addJsToWebView(javascript);
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {

                            }
                        });
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PRINT :", "ERROR");
        }
    }

    @JavascriptInterface
    public void viewHtml(String fileName) {
        fileName = fileName.substring(1, fileName.length());
        File f = new File(fileName);
        Uri uri = Uri.fromFile(f);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            intent.setDataAndType(Uri.fromFile(f), "text/html");
            this.activity.startActivity(Intent.createChooser(intent, "Open with"));
        } catch (Exception e) {
            //
        }
    }

    @JavascriptInterface
    public void sendEmail(String emailTo) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailTo));
        activity.startActivity(Intent.createChooser(emailIntent, "Select an app"));
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    @JavascriptInterface
    public void killApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @JavascriptInterface
    public void shareContentThroughIntent(final String content, final String contentType, final String viewId, final String identifier, final String type) {
        Log.e(identifier, type);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {

                    if (contentType.equals("file")) {
                        Log.d("TYPE FILE " + contentType, "FILE");

                    } else if (contentType.equals("text")) {
                        Log.d("TYPE TEXT " + contentType, "TEXT");

                    }

                    LinearLayout linearLayout = (LinearLayout) activity.findViewById(parseInt(viewId));

                    View.OnClickListener onclickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            logShareContentSuccessEvent(type, identifier);
                            if (view instanceof ImageView) {
                                ImageView imageV = (ImageView) view;
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                if (contentType.equals("file")) {
                                    Log.d("APP HANDLED TASK", imageV.getTag().toString());
                                    String contentName = content.substring(content.lastIndexOf("/") + 1);
                                    File file = new File(new File(Environment.getExternalStorageDirectory(), "Ecars/tmp/"), contentName);
                                    String authorities = BuildConfig.APPLICATION_ID + ".fileprovider";
                                    Uri contentUri = FileProvider.getUriForFile(context, authorities, file);

                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                    shareIntent.setType("application/zip");
//                                    logShareClickEvent("FILE");
                                } else if (contentType.equals("text")) {
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, content);
                                    shareIntent.setType("text/plain");
//                                    logShareClickEvent("LINK");
                                }

                                boolean isAppInstalled = appInstalledOrNot((String) imageV.getTag());

                                if (isAppInstalled) {
                                    shareIntent.setPackage((String) imageV.getTag());
                                    shareIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                    activity.getBaseContext().startActivity(shareIntent);

                                } else {
                                    Toast.makeText(activity.getBaseContext(), "App not installed", Toast.LENGTH_SHORT).show();
                                    try {
                                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + (String) imageV.getTag())));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + (String) imageV.getTag())));
                                    }
                                }

                            } else {
                                Log.d("APPLINKSHAREINTENTS", "NOT AN INSTANCE OF IMAGE VIEW");
                            }

                        }
                    };

                    List<String> packages = new ArrayList<>();

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);

                    if (contentType.equals("file")) {
                        File file = new File(content);
                        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(content));
                        sendIntent.setType("application/zip");
                    } else if (contentType.equals("text")) {
                        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                        sendIntent.setType("text/plain");
                    }

                    List<ResolveInfo> resolveInfoList = activity.getPackageManager()
                            .queryIntentActivities(sendIntent, 0);

                    ArrayList<String> appPackagesList = new ArrayList<>();
                    for (ResolveInfo resolveInfo : resolveInfoList) {
                        if (!appPackagesList.contains(resolveInfo.activityInfo.packageName)) {
                            if (!resolveInfo.activityInfo.packageName.contains("com.google.android.inputmethod"))

                                appPackagesList.add(resolveInfo.activityInfo.packageName);
                        }
                    }


                    PackageManager pm = activity.getApplicationContext().getPackageManager();
                    ApplicationInfo ai;
                    String applicationName;

                    TextView[] textView = new TextView[appPackagesList.size()];
                    ImageView[] imageView = new ImageView[appPackagesList.size()];
                    LinearLayout[] container = new LinearLayout[appPackagesList.size()];
                    int layoutHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, activity.getResources().getDisplayMetrics());
                    int layoutWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, activity.getResources().getDisplayMetrics());

                    ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
                    LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    containerParams.setMargins(0, 0, 20, 0);

                    int i = 0;

                    for (String packageName : appPackagesList) {
                        container[i] = new LinearLayout(activity);
                        container[i].setOrientation(LinearLayout.VERTICAL);
                        imageView[i] = new ImageView(activity);
                        Drawable icon = null;

                        try {
                            ai = pm.getApplicationInfo(packageName, 0);
                            icon = pm.getApplicationIcon(packageName);

                        } catch (final PackageManager.NameNotFoundException e) {
                            ai = null;
                        }

                        applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
                        Log.d("APP PACKAGE " + contentType, packageName);

                        imageView[i].setImageDrawable(icon);
                        imageView[i].setLayoutParams(params);
                        imageView[i].setOnClickListener(onclickListener);
                        imageView[i].setTag(packageName);

                        textView[i] = new TextView(activity);
                        textView[i].setText(applicationName);
                        textView[i].setGravity(Gravity.CENTER_HORIZONTAL);
                        textView[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);

                        container[i].addView(imageView[i]);
                        container[i].addView(textView[i]);
                        container[i].setGravity(Gravity.CENTER_HORIZONTAL);
                        container[i].setLayoutParams(containerParams);

                        linearLayout.addView(container[i]);

                        i++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public void closeApp() {
        activity.finish();
        //System.exit(0);
    }

    @JavascriptInterface
    public void switchToViewPagerIndex(String indexVal) {
        final int index = Integer.parseInt(indexVal);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(index, true);
            }
        });
    }

    private String decodeBase64(String data) throws UnsupportedEncodingException {
        byte[] dataText = Base64.decode(data, Base64.DEFAULT);
        String text = new String(dataText, "UTF-8");
        return text;
    }

    @JavascriptInterface
    public void callAPI(final String method, final String url, String dat, String header, final String callback) throws UnsupportedEncodingException {

        final String data = this.decodeBase64(dat);
        final String headers = this.decodeBase64(header);
//        Log.e("callAPI", url + " " +  data + " " + headers + " " + callback);
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected void onPostExecute(Object o) {
                if (o == null) {
//                    Log.e(LOG_TAG,"Please check if HTTP method (GET, POST, ..) is supported");
                }
                ApiResponse apiResponse = (ApiResponse) o;
//                Log.e("callAPI", "Response of API: "+ ((ApiResponse) o).getData());
                if (apiResponse.getStatusCode() == -1) {
                    String base64Data = Base64.encodeToString("{}".getBytes(), Base64.NO_WRAP);

                    String javascript = String.format("window.callUICallback('%s','%s','%s','%s','%s');", callback, "failure", base64Data, apiResponse.getStatusCode(), Base64.encode(url.getBytes(), Base64.NO_WRAP));
                    dynamicUI.addJsToWebView(javascript);
                } else {
                    String base64Data = null;
                    if (apiResponse.getData() == null) {
                        base64Data = "";
                    } else {
                        base64Data = Base64.encodeToString(apiResponse.getData(), Base64.NO_WRAP);
                        Log.e(TAG + "resp", base64Data);
                    }
                    String javascript = String.format("window.callUICallback('%s','%s','%s','%s','%s');", callback, "success", base64Data, apiResponse.getStatusCode(), Base64.encode(url.getBytes(), Base64.NO_WRAP));
                    Log.e(TAG, javascript);
                    dynamicUI.addJsToWebView(javascript);
                }

            }

            @Override
            protected ApiResponse doInBackground(Object[] params) {
                Log.e(LOG_TAG, "Now calling API :" + url);
                HashMap<String, String> h = new HashMap<String, String>();
                try {
                    JSONArray jsonHeadersArr = new JSONArray(headers);
                    Log.e("STATUS", "--------->1");
                    Iterator<String> keys;
                    for (int i = 0; i < jsonHeadersArr.length(); i += 1) {
                        JSONObject header = jsonHeadersArr.getJSONObject(i);
                        for (keys = header.keys(); keys.hasNext(); ) {
                            String key = keys.next();
                            h.put(key, header.getString(key));
                        }
                    }

                    if ("GET".equals(method)) {
                        HashMap<String, String> d = new HashMap<String, String>();
                        JSONObject jsonData = new JSONObject(data);
                        keys = jsonData.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            String value = jsonData.getString(key);
                            d.put(key, value);
                        }
                        Log.e(TAG + "GET", d.toString());
                        return org.sunbird.utils.RestClient.get(url, d, h, true);

                    } else if ("POST".equals(method)) {
                        Log.e("RestClient", "Caliing " + url);
                        return org.sunbird.utils.RestClient.post(url, data, h, true);
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.setStatusCode(-1);
                    apiResponse.setData(e.getLocalizedMessage().getBytes());
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    return apiResponse;
                }
            }
        };
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                asyncTask.execute();
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
//            Log.e("callAPI", "exception", e);
        }
    }

    @JavascriptInterface
    public String saveQrToInternalStorage(final String viewId, final String filename, final String callbackImage) {
        final String[] path = new String[]{null};
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // will work for layouts that support drawing apis
                    View layout = activity.findViewById(Integer.parseInt(viewId));
                    layout.setDrawingCacheEnabled(true);
                    layout.buildDrawingCache();
                    Bitmap map = layout.getDrawingCache();
                    path[0] = MediaStore.Images.Media.insertImage(activity.getContentResolver(), map, filename, "");

                    Log.d("imagePath", path[0]);

                    String javascript = String.format("window.callJSCallback('%s','%s');", callbackImage, "" + path[0]);
                    dynamicUI.addJsToWebView(javascript);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception", e);
                }
            }
        });
        return path[0];
    }

    @JavascriptInterface
    public void loadImageForQr() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.activity.startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @JavascriptInterface
    public String getFromSharedPrefs(String key) {
        return PreferenceManager.getDefaultSharedPreferences(activity).getString(key, "__failed");
    }

    @JavascriptInterface
    public void setInSharedPrefs(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString(key, value)
                .apply();
    }

    private byte[] gunzipContent(byte[] compressed) {
        final int BUFFER_SIZE = 1024;
        byte[] data = new byte[BUFFER_SIZE];
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(compressed);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
            int bytesRead;
            while ((bytesRead = gis.read(data)) != -1) {
                os.write(data, 0, bytesRead);
            }
            byte[] output = os.toByteArray();
            gis.close();
            is.close();
            os.close();
            return output;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception while gunzipping - ", e);

            return null;
        }
    }

    private String decryptJSFile(byte[] fileData) {
        try {
            byte[] encrypted = fileData;
            int FRAME_SIZE = 8;
            int KEY_SIZE = 8;
            byte[] keyValue = new byte[KEY_SIZE];
            byte[] encryptedWithoutKey = new byte[encrypted.length - KEY_SIZE];
            int keyCounter = 0;
            int encodedCounter = 0;
            int totalBytes = encrypted.length;
            keyValue[0] = encrypted[9];
            keyValue[1] = encrypted[19];
            keyValue[2] = encrypted[29];
            keyValue[3] = encrypted[39];
            keyValue[4] = encrypted[49];
            keyValue[5] = encrypted[59];
            keyValue[6] = encrypted[69];
            keyValue[7] = encrypted[79];
            for (int encryptedCounter = 0; encryptedCounter < totalBytes; encryptedCounter++) {
                if (encryptedCounter > 0 && encryptedCounter % 10 == 9 && keyCounter < KEY_SIZE) {
                    //Skip this byte
                    keyCounter++;
                } else {
                    //XOR Byte with KEY Byte
                    encryptedWithoutKey[encodedCounter] = (byte) ((int) encrypted[encryptedCounter] ^ (int) keyValue[encodedCounter % FRAME_SIZE]);
                    encodedCounter++;
                }
            }
            return new String(gunzipContent(encryptedWithoutKey));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while decrypting - ", e);
            return null;
        }
    }

    @JavascriptInterface
    public void setKey(String key, String value) {
        keyValueStore.write(key, value);
    }

    @JavascriptInterface
    public String getKey(String payload, String defaultValue) {
        return keyValueStore.getString(payload, defaultValue);
    }

    @JavascriptInterface
    public String decryptAndloadFile(String fileName) {
        Log.d(LOG_TAG, "Processing File - " + fileName);
        //Converting byte data to string is messing the gunzipping of file
        String data = null;
        byte[] fileData = null;
        try {
            fileData = FileUtil.getFileFromInternalStorageOrAssets(activity, fileName, "sunbird");
            if (fileName.endsWith(".jsa") && fileData != null) {
                try {
                    data = new String(decryptJSFile(fileData));
                } catch (Exception e) {
                    data = "";
                }
            }
        } catch (Exception e) {
            data = "";
        }
        return data;
    }

    @android.webkit.JavascriptInterface
    public void checkPermission(String callback) {
        try {
            JSONObject status = new JSONObject();
            status.put("PHONE_STATE_PERMISSION", checkPhoneStatePermission());
            status.put("SMS_PERMISSION", checkSMSPermission());
            dynamicUI.addJsToWebView("window.callUICallback(\"" + callback + "\", " + status.toString() + ")");
        } catch (Exception e) {
            dynamicUI.addJsToWebView("window.callUICallback(\"" + callback + "\", \"EXCEPTION\")");
        }
    }

    private boolean checkSMSPermission() {
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkPhoneStatePermission() {
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        String gotPermission = "";
        if (requestCode == STORAGE_PERMISSION_CODE) {
            gotPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            gotPermission = Manifest.permission.CAMERA;
        } else if (requestCode == PHONE_STATE_PERMISSION_CODE) {
            gotPermission = Manifest.permission.READ_PHONE_STATE;
        } else if (requestCode == COARSE_LOCATION_CODE) {
            gotPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
        }
        try {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("GRANTED GRANTED ", "");
                dynamicUI.addJsToWebView("window.callUICallback(\"" + permissionCallback + "\", \"" + gotPermission + "\");");
            } else {
                dynamicUI.addJsToWebView("window.callUICallback(\"" + permissionCallback + "\", \"ERROR\");");
            }
        } catch (Exception e) {
            dynamicUI.addJsToWebView("window.callUICallback(\"" + permissionCallback + "\", \"ERROR\");");
        }
    }

    @android.webkit.JavascriptInterface
    public void setPermissions(final String callback, final String permissionName) {

        REQUEST_CODE_PERMISSION = 0;

        if (permissionName.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            REQUEST_CODE_PERMISSION = STORAGE_PERMISSION_CODE;
        } else if (permissionName.equals(Manifest.permission.READ_PHONE_STATE)) {
            REQUEST_CODE_PERMISSION = PHONE_STATE_PERMISSION_CODE;
        } else if (permissionName.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            REQUEST_CODE_PERMISSION = COARSE_LOCATION_CODE;
        } else if (permissionName.equals(Manifest.permission.CAMERA)) {
            REQUEST_CODE_PERMISSION = CAMERA_PERMISSION_CODE;
        }

        Log.d("SET PERMISSIONS", "PERMISSIONS");

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                permissionCallback = callback;

                if (ContextCompat.checkSelfPermission(activity, permissionName) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("NO PREVIOUS", "ASK PERMISSION");

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionName)) {
                        Log.d("DENIED", "DENIED PERMANENTLY");
                        dynamicUI.addJsToWebView("window.callUICallback(\"" + permissionCallback + "\", \"" + "DeniedPermanently" + "\");");
                    } else {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{permissionName},
                                REQUEST_CODE_PERMISSION);
                    }

                } else {
                    Log.d("ALLOWED", "ALREADY GIVEN");
                    dynamicUI.addJsToWebView("window.callUICallback(\"" + permissionCallback + "\", \"" + permissionName + "\");");
                }
            }
        });
    }

    @JavascriptInterface
    public void showPermissionScreen() {
        activity.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
    }

    @JavascriptInterface
    public void hideKeyboard() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @JavascriptInterface
    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @android.webkit.JavascriptInterface
    public String getSymbol(String symbol) {
        switch (symbol) {
            case "tick":
                return "\u2713";
            case "rupee":
                return "\u20B9";
            default:
                return "symbol";
        }
    }

    @android.webkit.JavascriptInterface
    public void startWebSocket(String url) {
        ws.init(url);
    }

    @JavascriptInterface
    public void showSnackBarWithAction(String msg, String action, String callback) {
        Snackbar.make(activity.getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT).setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("snack clicked", "Hello");
            }
        }).show();
    }

    @JavascriptInterface
    public void showSnackBar(String msg) {
        Log.e("Snackbar:>", msg);
        Snackbar.make(activity.getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void showToast(String msg, String length) {
        int len;
        if (length == "long")
            len = Toast.LENGTH_LONG;
        else
            len = Toast.LENGTH_SHORT;

        Toast.makeText(activity, msg, len).show();
    }

    @JavascriptInterface
    public void addSwipeRefreshScrollView(final String id, final String callback) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    ScrollView s = (ScrollView) activity.findViewById(parseInt(id));
                    final SwipeRefreshLayout sWRlayout = new SwipeRefreshLayout(activity);
                    if (s.getParent() != null) {
                        ViewGroup tmp;
                        tmp = ((ViewGroup) s.getParent());
                        tmp.removeView(s);
                        sWRlayout.addView(s);
                        tmp.addView(sWRlayout);
                    }
                    sWRlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {

                            dynamicUI.addJsToWebView("window.callUICallback(\"" + callback + "\");");
                            sWRlayout.setRefreshing(false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @JavascriptInterface
    public void downloadImage(final String imgUrl) {
        String path = Environment.getExternalStorageDirectory() + File.separator + Constants.EXTERNAL_PATH;
        final File dir = new File(path);

        if (!(dir.exists() && dir.isDirectory())) {
            dir.mkdirs();
        } else {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    // something you know that will take a few seconds
                    try {
                        URL url = new URL(imgUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);

                        FileOutputStream stream = new FileOutputStream(dir + "/logo.png");
                        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                        myBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outstream);
                        byte[] byteArray = outstream.toByteArray();

                        stream.write(byteArray);
                        stream.close();

                        Log.d(TAG, "doInBackground: downloading complete");
                        setInSharedPrefs("logo_file_path", dir + "/logo.png");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "doInBackground: error while downloading");
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(final Void result) {
                    // continue what you are doing..
                }
            }.execute();
        }
    }

    @JavascriptInterface
    public void selectSpinnerItem(final String id, final String index) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Spinner spinner = (Spinner) activity.findViewById(Integer.parseInt(id));
                    spinner.setSelection(Integer.parseInt(index));
                } catch (Exception e) {
                    LOG.d(e.getMessage(), "spinner Exception");
                }
            }
        });
    }

    @JavascriptInterface
    public void saveData(String tag, String data) {
        Log.d(TAG, "saveData: called");
        SQLBlobStore.setData(activity.getBaseContext(), tag, data);
    }

    @JavascriptInterface
    public String getSavedData(String tag) {
        Log.d(TAG, "getSavedData: called");
        String ret = SQLBlobStore.getData(activity.getBaseContext(), tag);
        if (ret == null || ret == "undefined") ret = "__failed";
        return ret;
    }

    @JavascriptInterface
    public String getAppName() {
        return context.getString(R.string.app_name);
    }

    @JavascriptInterface
    public String getApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    @JavascriptInterface
    public void setParams() {
        GlobalApplication.getInstance().setParams();
    }

    @JavascriptInterface
    public void updateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void openSocialMedia(String url, String type) {
        Intent intent;
        if (url != "") intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        else return;
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void listViewAdapter(final String id, String text, int itemCount, String btnText, final String callback, final String buttonId, final int heightOfDivider) throws Exception {
        int listViewId = parseInt(id);
        final ListView listView = (ListView) activity.findViewById(listViewId);
        JSONArray jsonArray = new JSONArray(text);
        ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
        ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");
        ArrayList<Integer> viewTypeArrayList = jsonToArrayList(jsonArray, "viewType", "Int");
        final ListViewAdapter listViewAdapter = new ListViewAdapter(context, itemCount, valueArrayList, viewJSXArrayList, viewTypeArrayList, dynamicUI);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    listView.setAdapter(listViewAdapter);
                    listView.setDividerHeight(heightOfDivider);
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error in rendering listview");
                }
            }
        });
        if (btnText != null && !btnText.equals("")) {
            LinearLayout linearLayout = new LinearLayout(activity);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            Button btn = new Button(activity);
            linearLayout.setId(parseInt(buttonId));
            linearLayout.setBackgroundResource(R.drawable.layout_padding);
            btn.setText(btnText);
            btn.setLayoutParams(lp);
            btn.setTextColor(ContextCompat.getColor(activity, R.color.white));
            btn.setBackgroundResource(R.drawable.corner_radius);
            linearLayout.addView(btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cb = String.format("window.callJSCallback('%s');", callback);
                    dynamicUI.addJsToWebView(cb);
                    Log.e(TAG, "onClick: load more");
                }
            });
            listView.addFooterView(linearLayout);
        }
    }

    @JavascriptInterface
    public void appendToListView(final String id, String text, final int itemCount) throws Exception {
        int listViewId = parseInt(id);
        final ListView listView = (ListView) activity.findViewById(listViewId);
        JSONArray jsonArray = new JSONArray(text);
        final ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
        final ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");
        final ArrayList<Integer> viewTypeArrayList = jsonToArrayList(jsonArray, "viewType", "Int");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ListAdapter la = listView.getAdapter();
                    ListViewAdapter adapter;
                    if (la instanceof HeaderViewListAdapter) {
                        adapter = ((ListViewAdapter) ((HeaderViewListAdapter) la).getWrappedAdapter());
                    } else {
                        adapter = (ListViewAdapter) la;
                    }
                    adapter.addItemsToList(itemCount, valueArrayList, viewJSXArrayList, viewTypeArrayList);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error in adding item to listview");
                }
            }
        });
    }

    @JavascriptInterface
    public void hideFooterView(final String id, final String buttonId) {
        try {
            int listViewId = parseInt(id);
            final int buttonId1 = parseInt(buttonId);
            final ListView listView = (ListView) activity.findViewById(listViewId);
            final LinearLayout btn = (LinearLayout) activity.findViewById(buttonId1);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.removeFooterView(btn);
                }
            });
        } catch (Exception e) {
            Log.e("View!!", "Exception in hide footer view + " + e);
        }
    }

    @JavascriptInterface
    public void refreshAccessToken(final String callback) {
        final OkHttpClient client = new OkHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBody formBody = new FormEncodingBuilder()
                        .add("client_id", "android")
                        .add("grant_type", "refresh_token")
                        .add("refresh_token", getFromSharedPrefs("refresh_token"))
                        .build();
                Request request = new Request.Builder()
                        .url(BuildConfig.REDIRECT_BASE_URL + "/auth/realms/sunbird/protocol/openid-connect/token")
                        .post(formBody)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    String body = response.body().string();
                    Log.e(TAG, "refreshToken body: " + body);
                    final String javascript;
                    String base64Data = Base64.encodeToString(body.getBytes(), Base64.NO_WRAP);
                    if (response.isSuccessful()) {
                        javascript = String.format("window.callUICallback('%s','%s','%s','%s');", callback, "success", base64Data, response.code());
                    } else {
                        javascript = String.format("window.callUICallback('%s','%s','%s','%s');", callback, "failure", base64Data, response.code());
                    }
                    dynamicUI.addJsToWebView(javascript);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @JavascriptInterface
    public void getFocus(final String id) {
        int editTextViewId = parseInt(id);
        final EditText editText = (EditText) activity.findViewById(editTextViewId);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    editText.setFocusable(true);
                    editText.setFocusableInTouchMode(true);
                    editText.requestFocus();
                    showKeyboard();
                } catch (Exception e) {
                    Log.e("Error", " " + e);
                }
            }
        });
    }

    @JavascriptInterface
    public boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    @JavascriptInterface
    public boolean isChannelIdSet() {
        return BuildConfig.FILTER_CONTENT_BY_CHANNEL_ID;
    }

    @JavascriptInterface
    public String defaultChannelId() {
        return BuildConfig.CHANNEL_ID;
    }

    @JavascriptInterface
    public void fileUpload(String filePath, String apiToken, String userAccessToken, String userId, String cb) {
        File file = new File(filePath);
        Log.e(TAG, "fileUpload: " + file.exists());
        String resData = Util.postFile(BuildConfig.REDIRECT_BASE_URL + "/api/content/v1/media/upload", file, apiToken, userAccessToken, userId, cb);
        Log.e(TAG, "fileUpload: response: " + (resData == ""));
        if (resData == null || resData == "") {
            String javascript = String.format("window.callJSCallback('%s','%s');", cb, "__failed");
            dynamicUI.addJsToWebView(javascript);
        } else {
            String enc = Base64Util.encodeToString(resData.getBytes(), Base64Util.DEFAULT);
            String javascript = String.format("window.callJSCallback('%s','%s');", cb, enc);
            dynamicUI.addJsToWebView(javascript);
        }
    }

    @JavascriptInterface
    public void loadImageForProfileAvatar() {
        Intent intent = ImagePicker.getPickImageIntent(activity);
        activity.startActivityForResult(intent, MainActivity.IMAGE_CHOOSER_ID);
    }

    @JavascriptInterface
    public void registerFCM(String[] topics) {
        int len = topics.length;
        for (int i = len - 1; i >= 0; i--) {
            FirebaseMessaging.getInstance().subscribeToTopic(topics[i]);
        }
    }

    @JavascriptInterface
    public void unregisterFCM(String[] topics) {
        int len = topics.length;
        for (int i = len - 1; i >= 0; i--) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topics[i]);
        }
    }

    @JavascriptInterface
    public boolean checkIfDownloaded(final String path) {
        File file = new File(path);
        return file.exists();
    }

    @JavascriptInterface
    public void downloadAndOpen(final String url, final String path, final String callback, final int index) {
        downloadCallback = callback;
        Log.e("download!", "in jsfunction: " + url);
        File file = new File(path);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
        } else {
            mDownloadFileAsyncArray[index] = new DownloadFileAsync();
            mDownloadFileAsyncArray[index].execute(url, path);
        }
    }

    @JavascriptInterface
    public void cancelDownload(final int index, final String callback) {
        mDownloadFileAsyncArray[index].stopDownload();
        String javascript = String.format("window.callJSCallback('%s');", callback);
        dynamicUI.addJsToWebView(javascript);
    }

    @JavascriptInterface
    public void shareAnnouncement(String announcement) {
        try {
            JSONObject announcementData;
            announcementData = new JSONObject(announcement);
            String textToSend = "";
            try {
                textToSend = "Type: " + announcementData.getString("type")
                        + "\n" + "Title: " + announcementData.getString("title")
                        + "\n" + "Description: " + announcementData.getString("description");
                if (announcementData.has("links")) {
                    JSONArray links = announcementData.getJSONArray("links");
                    if (links.length() > 0) {
                        textToSend += "\nLinks: ";
                        for (int i = 0; i < links.length(); i++) {
                            textToSend += "" + links.getString(i) + ",";
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String authorities = BuildConfig.APPLICATION_ID + ".fileprovider";
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            shareIntent.setType("text/*");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Announcement");
            shareIntent.putExtra(Intent.EXTRA_TEXT, textToSend);
            JSONArray attachments = new JSONArray(announcementData.getString("attachments"));
            String path = "/storage/emulated/0/announcements/" + announcementData.getString("id") + "/";
            ArrayList<Uri> Uris = new ArrayList<>();
            for (int i = 0; i < attachments.length(); i++) {
                JSONObject dummy = attachments.getJSONObject(i);
                File file = new File(path + dummy.getString("name"));
                if (file.exists()) {
                    Uris.add(FileProvider.getUriForFile(context, authorities, file));
                }
            }
            if (!Uris.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                shareIntent.setType("*/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uris);
            }
            activity.startActivity(Intent.createChooser(shareIntent, "Share via.."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void openLink(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
    }

    class DownloadFileAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            startDownload(params[0], params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        public void startDownload(String url, String filePath) {
            mFileDownloader = new FileDownloader(url, mChangedListener, filePath);
            mFileDownloader.startDownload();
        }

        public void stopDownload() {
            mFileDownloader.stopDownloading();
        }
    }
}
