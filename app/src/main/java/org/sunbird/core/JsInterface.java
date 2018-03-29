package org.sunbird.core;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
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
import android.provider.Settings;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.HorizontalScrollView;
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
import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentFilterCriteria;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.Profile;
import org.ekstep.genieservices.commons.bean.enums.InteractionType;
import org.ekstep.genieservices.commons.bean.enums.JWTokenType;
import org.ekstep.genieservices.commons.utils.Base64Util;
import org.ekstep.genieservices.commons.utils.CryptoUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.utils.DeviceSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunbird.BuildConfig;
import org.sunbird.GlobalApplication;
import org.sunbird.R;
import org.sunbird.models.ApiResponse;
import org.sunbird.models.enums.ContentType;
import org.sunbird.telemetry.TelemetryAction;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryConstant;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.telemetry.TelemetryPageId;
import org.sunbird.telemetry.enums.ContextEnvironment;
import org.sunbird.telemetry.enums.CorrelationContext;
import org.sunbird.telemetry.enums.ImpressionType;
import org.sunbird.telemetry.enums.Mode;
import org.sunbird.telemetry.enums.ObjectType;
import org.sunbird.telemetry.enums.Workflow;
import org.sunbird.ui.HorizontalScroller;
import org.sunbird.ui.ListViewAdapter;
import org.sunbird.ui.MainActivity;
import org.sunbird.ui.MyRecyclerViewAdapter;
import org.sunbird.ui.SwipeToRefresh;
import org.sunbird.ui.TabLayout;
import org.sunbird.ui.ViewPagerAdapter;
import org.sunbird.utils.Constants;
import org.sunbird.utils.FileDownloader;
import org.sunbird.utils.FileDownloader.OnFileDownloadProgressChangedListener;
import org.sunbird.utils.FileHandler;
import org.sunbird.utils.GenieWrapper;
import org.sunbird.utils.ImagePicker;
import org.sunbird.utils.KeyValueStore;
import org.sunbird.utils.SQLBlobStore;
import org.sunbird.utils.Util;
import org.sunbird.utils.WebSocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
    private static String sharePkgVersion;
    private final int SMS_PERMISSION_CODE = 1, PHONE_STATE_PERMISSION_CODE = 2, STORAGE_PERMISSION_CODE = 3, COARSE_LOCATION_CODE = 4, CAMERA_PERMISSION_CODE = 5;
    private ListViewAdapter listViewAdapter = null;
    private MyRecyclerViewAdapter recylerViewAdapter = null;
    private String downloadCallback = "";
    private Context context;
    private MainActivity activity;
    private DynamicUI dynamicUI;
    private Map<String,String> mapId=new HashMap<String, String>();
    private Map<String,String[]> sectionMap = new HashMap<>();
    private Map<String,String[]> contentMap = new HashMap<>();
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

        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.LOGIN_INITIATE, TelemetryPageId.LOGIN, ContextEnvironment.HOME));

        CustomTabsIntent.Builder mBuilder = new CustomTabsIntent.Builder(getSession());
        CustomTabsIntent mIntent = mBuilder.build();

        String keyCloackAuthUrl = OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&scope=offline_access&client_id=" + CLIENT_ID;
        Log.e("URL HITTING:", keyCloackAuthUrl);
        mIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mIntent.launchUrl(activity, Uri.parse(keyCloackAuthUrl));

        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, TelemetryPageId.LOGIN, ContextEnvironment.HOME));
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

//    @JavascriptInterface
//    public void listViewAdapter(final String id, String text, int itemCount) throws Exception {
//        int listViewId = parseInt(id);
//        final ListView listView = (ListView) activity.findViewById(listViewId);
//        JSONArray jsonArray = new JSONArray(text);
//        ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
//        ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");
//        ArrayList<Integer> viewTypeArrayList = jsonToArrayList(jsonArray, "viewType", "Int");
//        listViewAdapter = new ListViewAdapter(context, itemCount, valueArrayList, viewJSXArrayList, viewTypeArrayList, dynamicUI);
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    listView.setAdapter(listViewAdapter);
//                } catch (Exception e) {
//                    Log.d("Exception", e.toString());
//                }
//            }
//        });
//    }

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
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return Util.isNetworkAvailable(activity);
    }

    @JavascriptInterface
    public void getContentImportStatus(final String identifier, final String callback) {
        genieWrapper.getImportStatus(identifier, callback);
    }

    @JavascriptInterface
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
    public void getContentDetails(String content_id, String callback, boolean returnFeedback) {
        Log.e("GEnie", "---------------------------------------------");
        genieWrapper.getContentDetails(callback, content_id, returnFeedback);
    }

    @JavascriptInterface
    public void addContentAccess(String contentId) {
        genieWrapper.addContentAccess(contentId);
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
    public void searchContent(String callback, String filterParams, String query, String type, int count, String[] keywords, boolean viewMoreClicked) {
//        Log.e("ser!", query);
        genieWrapper.searchContent(callback, filterParams, query, type, count, keywords, viewMoreClicked);
    }

    @JavascriptInterface
    public void importCourse(String course_id, String isChild, String[] callbacks) {
        Log.i("import course", "");
        genieWrapper.importCourse(course_id, isChild, callbacks);
    }

    @JavascriptInterface
    public void playContent(String contentDetails, String id, String pkgVersion, String cb, String rollUpData) {
        try {
            genieWrapper.playContent(contentDetails, cb, rollUpData);
            logContentPlayClicked(id, pkgVersion);
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

    int counter = 0;

    @JavascriptInterface
    public void syncTelemetry(int delay) {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            String option = getFromSharedPrefs("data_sync");
            int delay = 15000;
            @Override
            public void run() {

                if(option.equals("Over Wifi") && checkConnectionType().equals("wifi"))
                    genieWrapper.syncTelemetry();
                else if(option.equals("Always On")) {
                    if (checkConnectionType().equals("wifi")){
                        genieWrapper.syncTelemetry();
                    } else if(checkConnectionType().equals("mobile network")) {
                        counter ++;
                        if (counter % 2 == 0) {
                            genieWrapper.syncTelemetry();
                        }
                    }

                }
            }
        },0, 15000);

    }

    @JavascriptInterface
    public void syncTelemetryNow(final String callback){
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryPageId.SETTINGS_DATASYNC, TelemetryAction.MANUALSYNC_INITIATED, ContextEnvironment.HOME));
        genieWrapper.manualSyncTelemetry(callback);

    }

    @JavascriptInterface
    public void shareTelemetry(final String callback){
        genieWrapper.exportTelemetry(callback);
    }

    @JavascriptInterface
    public long getLastTelemetrySyncTime(){
       return genieWrapper.getLastSyncTime();
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
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryPageId.SIGNUP, TelemetryAction.SIGNUP_INITIATE, null, null));
    }

    @JavascriptInterface
    public void logSignUpSuccess() {
//        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.OTHER, TelemetryPageId.SIGNUP, TelemetryAction.SIGNUP_SUCCESS));
    }

    @JavascriptInterface
    public void logLogoutInitiate(String user_token) {
        Map<String, Object> vals = new HashMap<>();
        vals.put(TelemetryConstant.UID, user_token);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.LOGOUT_INITIATE, TelemetryPageId.LOGOUT, ContextEnvironment.HOME, vals));
    }

    @JavascriptInterface
    public void logLogoutSuccess(String user_token) {
        Map<String, Object> vals = new HashMap<>();
        vals.put(TelemetryConstant.UID, user_token);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.OTHER, TelemetryAction.LOGOUT_SUCCESS, TelemetryPageId.LOGOUT, ContextEnvironment.HOME, vals));
    }

    @JavascriptInterface
    public void logResourceDetailScreenEvent(String identifier, String pkgVersion, String onDevice) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.DETAIL, null, TelemetryPageId.CONTENT_DETAIL, ContextEnvironment.HOME, identifier, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
        Map<String, Object> params = new HashMap<>();
        params.put(TelemetryConstant.PRESENT_ON_DEVICE, onDevice);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildLogEvent(TelemetryPageId.CONTENT_DETAIL, ImpressionType.DETAIL, TelemetryPageId.CONTENT_DETAIL, ContextEnvironment.HOME, params));
    }

    @JavascriptInterface
    public void logCourseDetailScreenEvent(String identifier, String pkgVersion, String onDevice) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.DETAIL, null, TelemetryPageId.COURSE_HOME, ContextEnvironment.HOME, identifier, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
        Map<String, Object> params = new HashMap<>();
        params.put(TelemetryConstant.PRESENT_ON_DEVICE, onDevice);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildLogEvent(TelemetryPageId.CONTENT_DETAIL, ImpressionType.DETAIL, TelemetryPageId.CONTENT_DETAIL, ContextEnvironment.HOME, params));
    }

    @JavascriptInterface
    public void logRollupEvent(String type, String l1, String l2, String l3, String l4){
        String env = ContextEnvironment.HOME;
        String pageId = TelemetryPageId.CONTENT_DETAIL;
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, null, pageId, env, l1, l2, l3, l4));

    }

    @JavascriptInterface
    public void logContentDetailScreenEvent(String identifier, String pkgVersion) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.DETAIL, null, TelemetryPageId.CONTENT_DETAIL, ContextEnvironment.HOME, identifier, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildLogEvent(TelemetryPageId.CONTENT_DETAIL, ImpressionType.DETAIL, TelemetryPageId.CONTENT_DETAIL, ContextEnvironment.HOME, null));
    }

    @JavascriptInterface
    public void logCorrelationPageEvent(String type, String id, String ctype) {
        switch (type) {
            case "HOME":
                Util.setCorrelationContext(CorrelationContext.HOME_PAGE);
                break;

            case "COURSES":
                Util.setCorrelationContext(CorrelationContext.COURSE_PAGE);
                break;

            case "LIBRARY":
                Util.setCorrelationContext(CorrelationContext.RESOURCE_PAGE);
                break;

            case "SPLASHSCREEN":
                Util.setCorrelationContext(CorrelationContext.NONE);
                break;
        }

        Util.setCorrelationType(ctype);
        Util.setCorrelationId(id);
    }

    @JavascriptInterface
    public void logContentClickEvent(String type, int position, String phrase, String contentId, String pkgVersion) {
        String pageId = "";
        switch (type) {
            case "HOME":
                pageId = TelemetryPageId.HOME;
                break;

            case "COURSES":
                pageId = TelemetryPageId.COURSES;
                break;

            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                break;
        }
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.POSITION_CLICKED, position);
        eksMap.put(TelemetryConstant.SEARCH_PHRASE, phrase);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.CONTENT_CLICKED, pageId, ContextEnvironment.HOME, eksMap, contentId, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
    }

    @JavascriptInterface
    public void logCardClickEvent(String type, int position, String sectionName, String contentId, String pkgVersion) {
        String pageId = "";
        switch (type) {
            case "HOME":
                pageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                pageId = TelemetryPageId.COURSES;
                break;
            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                break;
        }
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.POSITION_CLICKED, position);
        eksMap.put(TelemetryConstant.SEARCH_PHRASE, sectionName);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.CONTENT_CLICKED, pageId, ContextEnvironment.HOME, eksMap, contentId, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
    }
    @JavascriptInterface
    public void logSettingsClickedEvent(String type) {
        String pageId = "";
        String action = "";
        switch (type) {
            case "SETTINGS":
                pageId = TelemetryPageId.PROFILE;
                action = TelemetryAction.SETTINGS_CLICKED;
                break;
            case "SETTINGS_DATASYNC":
                pageId = TelemetryPageId.SETTINGS;
                action = TelemetryAction.DATA_SYNC_CLICKED;
                break;
            case "SETTINGS_LANGUAGE":
                pageId = TelemetryPageId.SETTINGS;
                action = TelemetryAction.LANGUAGE_CLICKED;
                break;
            case "SETTINGS_SUPPORT":
                pageId = TelemetryPageId.SETTINGS;
                action = TelemetryAction.SUPPORT_CLICKED;
                break;
            case "SETTINGS_SHARE":
                pageId = TelemetryPageId.SETTINGS;
                action = TelemetryAction.SHARE_APP_CLICKED;
                break;
            case "SETTINGS_DEVICE_TAGS":
                pageId = TelemetryPageId.SETTINGS;
                action = TelemetryAction.DEVICE_TAGS_CLICKED;
                break;
            case "ABOUT_APP":
                pageId = TelemetryPageId.SETTINGS;
                action = TelemetryAction.ABOUT_APP_CLICKED;
                break;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, action, pageId, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void logLanguageChangeSettingEvent(String prev,String curr){
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.PREVIOUS_LANGUAGE, prev);
        eksMap.put(TelemetryConstant.CURRENT_LANGUAGE, curr);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.LANGUAGE_SETTINGS_SUCCESS, TelemetryPageId.SETTINGS_LANGUAGE, ContextEnvironment.HOME, eksMap));
    }

    @JavascriptInterface
    public void logShareAppEvent(){
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.SHARE_APP_INITIATED, TelemetryPageId.SETTINGS, ContextEnvironment.HOME));
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.OTHER, TelemetryAction.SHARE_APP_SUCCESS, TelemetryPageId.SETTINGS, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void logSettingsScreenEvent(String type){
        String pageId = "";
        switch (type){
            case "SETTINGS":
                pageId = TelemetryPageId.SETTINGS;
                break;
            case "SETTINGS_DATASYNC":
                pageId = TelemetryPageId.SETTINGS_DATASYNC;
                break;
            case "SETTINGS_LANGUAGE":
                pageId = TelemetryPageId.SETTINGS_LANGUAGE;
                break;
            case "SETTINGS_DEVICE_TAGS":
                pageId = TelemetryPageId.SETTINGS_DEVICE_TAGS;
                break;
            case "ABOUT_APP":
                pageId = TelemetryPageId.ABOUT_APP;
                break;
            case "SIGNIN_OVERLAY":
                pageId = TelemetryPageId.SIGNIN_OVERLAY;
                break;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, null, pageId, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void logGuestEvent(String type){
        String pageId = "";
        String action = "";
        switch (type){
            case "LOGIN":
                pageId = TelemetryPageId.LOGIN;
                action = TelemetryAction.BROWSE_AS_GUEST_CLICKED;
                break;
            case "HOME":
                pageId = TelemetryPageId.HOME;
                action = TelemetryAction.SIGNIN_OVERLAY_CLICKED;
                break;
            case "COURSE":
                pageId = TelemetryPageId.COURSES;
                action = TelemetryAction.SIGNIN_OVERLAY_CLICKED;
                break;
            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                action = TelemetryAction.SIGNIN_OVERLAY_CLICKED;
                break;
            case "CONTENT_DETAIL":
                pageId = TelemetryPageId.CONTENT_DETAIL;
                action = TelemetryAction.SIGNIN_OVERLAY_CLICKED;
                break;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, action, pageId, ContextEnvironment.HOME));

    }

    @JavascriptInterface
    public void logViewAllClickEvent(String type, String sectionName) {
        String pageId = TelemetryPageId.HOME, subType = "";
        switch (type) {
            case "HOME":
                pageId = TelemetryPageId.HOME;
                break;

            case "COURSES":
                pageId = TelemetryPageId.COURSES;
                break;

            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                break;
        }

        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.SECTION_NAME, sectionName);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.VIEWALL_CLICKED, pageId, ContextEnvironment.HOME, eksMap, Util.getCorrelationList()));
    }

    @JavascriptInterface
    public void logQRIconClicked() {
        Log.e(TAG, "logQRIconClicked: ");
        String stageId = TelemetryPageId.HOME;
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.QRCodeScanClicked, stageId, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void logQRScanInitiated() {
        Log.e(TAG, "logQRScanInitiated: ");
        String stageId = TelemetryPageId.QRCodeScanner;
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, TelemetryAction.QRCodeScanInitiate, stageId, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void logQRScanSuccess(String scannedData, String action) {
        Log.e(TAG, "logQRScanSuccess: ");
        String stageId = TelemetryPageId.QRCodeScanner;
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(TelemetryConstant.NETWORK_AVAILABLE, isNetworkAvailable());
        valueMap.put(TelemetryConstant.SCANNED_DATA, scannedData);

        if (action.equals(TelemetryConstant.CONTENT_DETAIL))
            valueMap.put(TelemetryConstant.ACTION, TelemetryConstant.CONTENT_DETAIL);
        else if (action.equals(TelemetryConstant.SEARCH_RESULT))
            valueMap.put(TelemetryConstant.ACTION, TelemetryConstant.SEARCH_RESULT);
        else if (action.equals(TelemetryConstant.UNKNOWN))
            valueMap.put(TelemetryConstant.ACTION, TelemetryConstant.UNKNOWN);

        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.OTHER, TelemetryAction.QRCodeScanSuccess, stageId, ContextEnvironment.HOME, valueMap));
    }

    @JavascriptInterface
    public void logQRScanCancelled() {
        Log.e(TAG, "logQRScanCancelled: ");
        String stageId = TelemetryPageId.QRCodeScanner;
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.OTHER, TelemetryAction.QRCodeScanCancelled, stageId, ContextEnvironment.HOME));
    }


    @JavascriptInterface
    public void logTabClickEvent(String type) {
        String pageId = TelemetryPageId.HOME, subType = "";
        switch (type) {
            case "HOME":
                pageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                pageId = TelemetryPageId.COURSES;
                break;
            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                break;
            case "GROUPS":
                pageId = TelemetryPageId.GROUPS;
                break;
            case "PROFILE":
                pageId = TelemetryPageId.PROFILE;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.TAB_CLICKED, pageId, ContextEnvironment.HOME));
    }

//    @JavascriptInterface
//    public void logShareClickEvent(String type) {
//        String subType;
//        if (type.equals("LINK")) {
//            subType = TelemetryAction.SHARE_LINK;
//        } else {
//            subType = TelemetryAction.SHARE_FILE;
//        }
//        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryPageId.SHARE, subType, null, null));
//    }

    @JavascriptInterface
    public void logTabScreenEvent(String type) {
        String pageId = TelemetryPageId.HOME;
        switch (type) {
            case "HOME":
                pageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                pageId = TelemetryPageId.COURSES;
                break;
            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                break;
            case "GROUPS":
                pageId = TelemetryPageId.GROUPS;
                break;
            case "PROFILE":
                pageId = TelemetryPageId.PROFILE;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, pageId, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void logShareContentSuccessEvent(String type, String contentType, String identifier) {
        Map<String, Object> vals = new HashMap<>();
        vals.put(TelemetryConstant.CONTENT_TYPE, contentType);
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.SHARE_COURSE_SUCCESS, TelemetryPageId.COURSE_HOME, ContextEnvironment.HOME, vals, identifier, ObjectType.CONTENT, sharePkgVersion, Util.getCorrelationList()));
        } else if (type.equals("LIBRARY")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.SHARE_LIBRARY_SUCCESS, TelemetryPageId.CONTENT_DETAIL, ContextEnvironment.HOME, vals, identifier, ObjectType.CONTENT, sharePkgVersion, Util.getCorrelationList()));
        }
    }

    @JavascriptInterface
    public void logShareContentInitiateEvent(String type, String contentType, String identifier, String pkgVersion) {
        sharePkgVersion = pkgVersion;
        Map<String, Object> vals = new HashMap<>();
        vals.put(TelemetryConstant.CONTENT_TYPE, contentType);
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.SHARE_COURSE_INITIATED, TelemetryPageId.COURSE_HOME, ContextEnvironment.HOME, vals, identifier, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
        } else if (type.equals("LIBRARY")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.SHARE_LIBRARY_INITIATED, TelemetryPageId.CONTENT_DETAIL, ContextEnvironment.HOME, vals, identifier, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
        }
    }

    @JavascriptInterface
    public void logshareScreenEvent(String contentId, String pkgVersion) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, null, TelemetryPageId.SHARE_CONTENT, ContextEnvironment.HOME, contentId, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
    }

    @JavascriptInterface
    public void logsplashScreenEvent() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, TelemetryPageId.SPLASH_SCREEN, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void logFlagClickInitiateEvent(String type, String reason, String comment, String contentId, String contentType, String pkgVersion) {
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.REASON, reason);
        eksMap.put(TelemetryConstant.COMMENT, comment);
        eksMap.put(TelemetryConstant.CONTENT_TYPE, contentType);
        String pageId = "";
        if (type.equals("COURSES")) {
            pageId = TelemetryPageId.COURSE_HOME;
        } else if (type.equals("LIBRARY")) {
            pageId = TelemetryPageId.CONTENT_DETAIL;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.FLAG_INITIATE, pageId, ContextEnvironment.HOME, eksMap, contentId, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
    }

    @JavascriptInterface
    public void logFlagScreenEvent(String type, String contentId, String pkgVersion) {
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, null, TelemetryPageId.FLAG_CONTENT, ContextEnvironment.HOME, contentId, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
        } else if (type.equals("LIBRARY")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, null, TelemetryPageId.FLAG_CONTENT, ContextEnvironment.HOME, contentId, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
        }
    }

    @JavascriptInterface
    public void logFlagStatusEvent(String identifier, String type, boolean status, String pkgVersion) {
        String pageId = "", subType;
        if (type.equals("COURSES")) {
            pageId = TelemetryPageId.FLAG_CONTENT;
        } else if (type.equals("LIBRARY")) {
            pageId = TelemetryPageId.FLAG_CONTENT;
        }

        if (status) {
            subType = TelemetryAction.FLAG_SUCCESS;
        } else {
            subType = TelemetryAction.FLAG_FAILED;
        }

        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.CONTENT_TYPE, type);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.OTHER, pageId, ContextEnvironment.HOME, subType, eksMap, identifier, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
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
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, null, TelemetryPageId.COURSE_PAGE_FILTER, ContextEnvironment.HOME, Util.getCorrelationList()));
        } else if (type.equals("LIBRARY")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, null, TelemetryPageId.LIBRARY_PAGE_FILTER, ContextEnvironment.HOME, Util.getCorrelationList()));
        }
    }

    @JavascriptInterface
    public void logPageFilterClickEvent(String type) {
        if (type.equals("COURSES")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.CANCEL, TelemetryPageId.COURSE_PAGE_FILTER, ContextEnvironment.HOME));
        } else if (type.equals("LIBRARY")) {
            TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.CANCEL, TelemetryPageId.LIBRARY_PAGE_FILTER, ContextEnvironment.HOME));
        }
    }

    @JavascriptInterface
    public void logAnnouncementListShow() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, TelemetryPageId.ANNOUNCEMENT_LIST, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void logAnnouncementClicked(String from, String announcementId, String pos) {
        String pageId = TelemetryPageId.ANNOUNCEMENT_LIST;
        if (from.equals("HOME")) pageId = TelemetryPageId.HOME;
        Map<String, Object> eksMap = new HashMap<>();
        eksMap.put(TelemetryConstant.POSISTION, pos);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.ANNOUNCEMENT_CLICKED, pageId, ContextEnvironment.HOME, eksMap, announcementId, ObjectType.ANNOUNCEMENT, null));
    }

    @JavascriptInterface
    public void logAnnouncementDeatilScreen(String announcementId) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.VIEW, null, TelemetryPageId.ANNOUNCEMENT_DETAIL, announcementId, ObjectType.ANNOUNCEMENT, null, null));
    }

    @JavascriptInterface
    public void logContentPlayClicked(String id, String pkgVersion) {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, TelemetryAction.CONTENT_PLAY, TelemetryPageId.CONTENT_DETAIL, ContextEnvironment.HOME, null, id, ObjectType.CONTENT, pkgVersion, Util.getCorrelationList()));
    }

    @JavascriptInterface
    public void startEventLog(String type, String objId, String pkgVersion) {
        //TODO add lessonPlan
//        String eType = Workflow.CONTENT;
//        switch (type.toLowerCase()) {
//            case "course":
//                eType = Workflow.COURSE;
//                break;
//            case "textbook":
//                eType = Workflow.TEXTBOOK;
//                break;
//            case "content":
//                eType = Workflow.CONTENT;
//                break;
//            case "collection":
//                eType = Workflow.COLLECTION;
//                break;
//        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildStartEvent(context, type.toLowerCase(), Mode.PLAY, null, ContextEnvironment.HOME, objId, ObjectType.CONTENT, pkgVersion));
    }

    @JavascriptInterface
    public void endEventLog(String type, String objId, String pkgVersion) {
        String eType = Workflow.CONTENT;
        switch (type.toLowerCase()) {
            case "course":
                eType = Workflow.COURSE;
                break;
            case "textbook":
                eType = Workflow.TEXTBOOK;
                break;
            case "content":
                eType = Workflow.CONTENT;
                break;
            case "collection":
                eType = Workflow.COLLECTION;
                break;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildEndEvent(0, eType, Mode.PLAY, null, ContextEnvironment.HOME, objId, ObjectType.CONTENT, pkgVersion));
    }

    @JavascriptInterface
    public void explicitSearch(String page, String type) {
        String pageId = TelemetryPageId.HOME, subType = TelemetryAction.SEARCH_BUTTON_CLICKED;
        switch (page) {
            case "COMBINED":
                pageId = TelemetryPageId.HOME;
                break;
            case "COURSE":
                pageId = TelemetryPageId.COURSES;
                break;
            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                break;
            case "PROFILE":
                pageId = TelemetryPageId.PROFILE;
                break;
        }
        switch (type) {
            case "SEARCH":
                subType = TelemetryAction.SEARCH_BUTTON_CLICKED;
                break;
            case "FILTER":
                subType = TelemetryAction.FILTER_BUTTON_CLICKED;
                break;
        }
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInteractEvent(InteractionType.TOUCH, subType, pageId, ContextEnvironment.HOME));
    }

    @JavascriptInterface
    public void stopEventBus(String id) {
        Log.e("stop event bus", "");
        genieWrapper.stopEventBus(id);
    }

    @JavascriptInterface
    public void stopTelemetryEvent() {
        genieWrapper.stopTelemetryEvent();
    }

    @JavascriptInterface
    public void exportEcar(String contentId, String callback) {
        Log.d("CONTENT ID", contentId);
        genieWrapper.exportEcar(contentId, callback);
    }

    @JavascriptInterface
    public void importEcar(String filePath, String[] callbacks) {
        genieWrapper.importEcarFile(filePath, callbacks);
    }

    @JavascriptInterface
    public void setProfile(String uid, boolean isGuestMode, String setProfileCb) {
        genieWrapper.getAllUserProfiles(uid, isGuestMode, setProfileCb);
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
                            logShareContentSuccessEvent(type, contentType, identifier);
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
                                    activity.startActivity(shareIntent);

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

    @JavascriptInterface
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
        activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
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

    @JavascriptInterface
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

    @JavascriptInterface
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
                    final SwipeToRefresh sWRlayout = new SwipeToRefresh(activity);
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
    public void listViewAdapter(final String id, final String text, final int itemCount, final String btnText, final String callback, final String buttonId, final int heightOfDivider) throws Exception {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int listViewId = parseInt(id);
                    final ListView listView = (ListView) activity.findViewById(listViewId);
                    final JSONArray jsonArray = new JSONArray(text);
                    ArrayList<String> viewJSXArrayList = jsonToArrayList(jsonArray, "view", "String");
                    final ArrayList<String> valueArrayList = jsonToArrayList(jsonArray, "value", "String");
                    ArrayList<Integer> viewTypeArrayList = jsonToArrayList(jsonArray, "viewType", "Int");

                    Log.e(TAG, "listViewAdapter: isNull " + listViewAdapter);
                    ListViewAdapter listViewAdapter = new ListViewAdapter(context, itemCount, valueArrayList, viewJSXArrayList, viewTypeArrayList, dynamicUI);
                    listView.setAdapter(listViewAdapter);
                    listView.setDividerHeight(heightOfDivider);

                    if(jsonArray.getJSONObject(0).has("name")) {
                        final ArrayList<String> nameArrayList = jsonToArrayList(jsonArray, "name", "String");
                        final ArrayList<String> idArrayList = jsonToArrayList(jsonArray, "id", "String");
                        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(AbsListView absListView, int i) {
                                Log.d("ListView", "ScrollStop");
                                int first = listView.getFirstVisiblePosition();
                                int last = listView.getLastVisiblePosition();
                                while (first <= last) {
                                    String value[] = new String[2];
                                    if (first < idArrayList.size() && first < nameArrayList.size()) {
                                        value[0] = idArrayList.get(first);
                                        value[1] = nameArrayList.get(first);
                                        if (!contentMap.containsKey(first))
                                            contentMap.put(String.valueOf(first), value);
                                    }
                                    first++;
                                }
                            }

                            @Override
                            public void onScroll(AbsListView absListView, int i, int i1, int i2) {


                            }
                        });
                    }

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
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error in rendering listview, err -> ");
                    e.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public void appendToListView(final String id, String text, final int itemCount) throws Exception {
        int listViewId = parseInt(id);
        final ListView listView = (ListView) activity.findViewById(listViewId);
        final JSONArray jsonArray = new JSONArray(text);
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

                    if(jsonArray.getJSONObject(0).has("name")) {
                        final ArrayList<String> nameArrayList = jsonToArrayList(jsonArray, "name", "String");
                        final ArrayList<String> idArrayList = jsonToArrayList(jsonArray, "id", "String");
                        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(AbsListView absListView, int i) {
                                Log.d("ListView", "ScrollStop");
                                int first = listView.getFirstVisiblePosition();
                                int last = listView.getLastVisiblePosition();
                                while (first <= last) {
                                    String value[] = new String[2];
                                    if (first < idArrayList.size() && first < nameArrayList.size()) {
                                        value[0] = idArrayList.get(first);
                                        value[1] = nameArrayList.get(first);
                                        if (!contentMap.containsKey(first))
                                            contentMap.put(String.valueOf(first), value);
                                    }
                                    first++;
                                }

                            }

                            @Override
                            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                            }
                        });

                    }
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

                    JSONObject res = new JSONObject(body);
                    String access_token = res.get("access_token").toString();
                    String userToken = Util.parseUserTokenFromAccessToken(access_token);
                    setInSharedPrefs("user_token", userToken);

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
                            if(i == (links.length()-1))
                                textToSend += "" + links.getString(i);
                            else
                                textToSend += "" + links.getString(i) + ", ";

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

    @JavascriptInterface
    public long epochTime() {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(activity.getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appFile = appInfo.sourceDir;
        long installed = new File(appFile).lastModified();
        return installed;
    }

    @JavascriptInterface
    public String checkConnectionType() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return "none";
        } else {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return "wifi";
            else
                return "mobile network";
        }
    }

    @JavascriptInterface
    public void downloadAllContent(String cb_id, String[] indetifierList, String[] callbacks) {
        genieWrapper.downloadAllContent(cb_id, indetifierList, callbacks);
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

    @JavascriptInterface
    public String getLocalLang() {
        return Locale.getDefault().toString();
    }

    @JavascriptInterface
    public String getBoards() {
        return genieWrapper.getBoards();
    }

    @JavascriptInterface
    public String getMediums() {
        return genieWrapper.getMediums();
    }

    @JavascriptInterface
    public String getSubjects() {
        return genieWrapper.getSubjects();
    }

    @JavascriptInterface
    public String getGrades() {
        return genieWrapper.getGrades();
    }

    @JavascriptInterface
    public String getCurrentProfileData() {
        return genieWrapper.getCurrentProfileData();
    }

    @JavascriptInterface
    public void updateProfile(String handle, String[] medium, String[] grade, String[] board, String[] subjects) {
        genieWrapper.updateProfile(handle, medium, grade, board, subjects);
    }

    @JavascriptInterface
    public void shareSupportFile(String layoutId) {
        File supportFile = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.EXTERNAL_PATH + File.separator + SUNBIRD_SUPPORT_FILE);
        Uri fileUri = FileProvider.getUriForFile(activity.getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", supportFile);

        final Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        List<ResolveInfo> resolveInfoList = activity.getPackageManager().queryIntentActivities(sendIntent, 0);

        final View.OnClickListener onclickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof ImageView) {
                    ImageView imageV = (ImageView) view;
                    String[] data = (String[]) imageV.getTag();
                    boolean isAppInstalled = appInstalledOrNot(data[0]);

                    if (isAppInstalled) {
                        sendIntent.setPackage(data[0]);
                        activity.startActivity(sendIntent);
                    } else {
                        Toast.makeText(activity.getBaseContext(), "App not installed", Toast.LENGTH_SHORT).show();
                        try {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + data[0])));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + data[0])));
                        }
                    }
                } else {
                    Log.d("APPLINKSHAREINTENTS", "NOT AN INSTANCE OF IMAGE VIEW");
                }
            }
        };

        inflateLayouts(layoutId, resolveInfoList, onclickListener);
    }

    @JavascriptInterface
    public void supportEmail(String layoutId) {
        File supportFile = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.EXTERNAL_PATH + File.separator + SUNBIRD_SUPPORT_FILE);
        Uri fileUri = FileProvider.getUriForFile(activity.getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", supportFile);

        //ACTION_SENDTO invokes email clients, but cannot attach file.
        //Hence, create intent with ACTION_SENDTO to retrieve all email clients and then
        // use ACTION_SEND intent to send attached file to the chosen email client.
        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + activity.getString(R.string.supportEmail)));
        List<ResolveInfo> resolveInfoList = activity.getPackageManager().queryIntentActivities(emailIntent, 0);


        final Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_EMAIL,new String[] { activity.getString(R.string.supportEmail) });
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Email");
        sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        final View.OnClickListener onclickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof ImageView) {
                    ImageView imageV = (ImageView) view;
                    String[] data = (String[]) imageV.getTag();
                    boolean isAppInstalled = appInstalledOrNot(data[0]);

                    if (isAppInstalled) {
                        sendIntent.setComponent(new ComponentName(data[0], data[1]));
                        activity.startActivity(sendIntent);
                    } else {
                        Toast.makeText(activity.getBaseContext(), "App not installed", Toast.LENGTH_SHORT).show();
                        try {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + data[0])));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + data[0])));
                        }
                    }
                } else {
                    Log.d("APPLINKSHAREINTENTS", "NOT AN INSTANCE OF IMAGE VIEW");
                }
            }
        };

        inflateLayouts(layoutId, resolveInfoList, onclickListener);
    }

    @JavascriptInterface
    public void shareApk(String layoutId) {

        ApplicationInfo app = context.getApplicationInfo();
        String filePath = app.sourceDir;
        final Intent intent = new Intent(Intent.ACTION_SEND);

        // MIME of .apk is "application/vnd.android.package-archive".
        // but Bluetooth does not accept this. Let's use "*/*" instead.
        intent.setType("*/*");

        // Append file and send Intent
        File originalApk = new File(filePath);

        try {
            //Make new directory in new location
            File tempFile = new File(activity.getExternalCacheDir() + "/ExtractedApk");
            //If directory doesn't exists create new
            if (!tempFile.isDirectory())
                if (!tempFile.mkdirs())
                    return;
            //Get application's name and convert to lowercase
            tempFile = new File(tempFile.getPath() + "/" + activity.getString(R.string.app_name) + "_" + getAppVersion() + ".apk");
            //If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return;
                }
            }
            //Copy file to new location
            InputStream in = new FileInputStream(originalApk);
            OutputStream out = new FileOutputStream(tempFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            System.out.println("File copied.");

            Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", tempFile);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);

            final View.OnClickListener onclickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view instanceof ImageView) {
                        ImageView imageV = (ImageView) view;
                        String[] data = (String[]) imageV.getTag();
                        boolean isAppInstalled = appInstalledOrNot(data[0]);

                        if (isAppInstalled) {
                            intent.setPackage(data[0]);
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);

                        } else {
                            Toast.makeText(activity.getBaseContext(), "App not installed", Toast.LENGTH_SHORT).show();
                            try {
                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + data[0])));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + data[0])));
                            }
                        }

                    } else {
                        Log.d("APPLINKSHAREINTENTS", "NOT AN INSTANCE OF IMAGE VIEW");
                    }

                }
            };

            List<ResolveInfo> resolveInfoList = activity.getPackageManager()
                    .queryIntentActivities(intent, 0);


            inflateLayouts(layoutId, resolveInfoList, onclickListener);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void inflateLayouts(final String layoutId, final List<ResolveInfo> resolveInfoList, final View.OnClickListener onClickListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final LinearLayout linearLayout = (LinearLayout) activity.findViewById(parseInt(layoutId));
                ArrayList<String> appPackagesList = new ArrayList<>();
                for (ResolveInfo resolveInfo : resolveInfoList) {
                    if (!appPackagesList.contains(resolveInfo.activityInfo.packageName)) {
                        appPackagesList.add(resolveInfo.activityInfo.packageName);
                    }
                }
                PackageManager pm = activity.getApplicationContext().getPackageManager();
                ApplicationInfo ai;
                String applicationName;
                linearLayout.removeAllViews();
                TextView[] textView = new TextView[appPackagesList.size()];
                ImageView[] imageView = new ImageView[appPackagesList.size()];
                LinearLayout[] container = new LinearLayout[appPackagesList.size()];
                int layoutHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, activity.getResources().getDisplayMetrics());
                int layoutWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, activity.getResources().getDisplayMetrics());

                ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
                LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                containerParams.setMargins(0, 0, 20, 0);
                for (int i = 0; i < appPackagesList.size(); i++) {
                    String packageName = appPackagesList.get(i);
                    String name = resolveInfoList.get(i).activityInfo.name;
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

                    imageView[i].setImageDrawable(icon);
                    imageView[i].setLayoutParams(params);
                    imageView[i].setOnClickListener(onClickListener);
                    String[] data = new String[2];
                    data[0] = packageName;
                    data[1] = name;
                    imageView[i].setTag(data);

                    textView[i] = new TextView(activity);
                    textView[i].setText(applicationName);
                    textView[i].setGravity(Gravity.CENTER_HORIZONTAL);
                    textView[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);

                    container[i].addView(imageView[i]);
                    container[i].addView(textView[i]);
                    container[i].setGravity(Gravity.CENTER_HORIZONTAL);
                    container[i].setLayoutParams(containerParams);

                    linearLayout.addView(container[i]);
                }
            }
        });
    }

    @JavascriptInterface
    public int isViewVisible(View view,ScrollView scrollView){
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (view.getLocalVisibleRect(scrollBounds)) {
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            double visible = rect.width() * rect.height();
            double total = view.getWidth() * view.getHeight();
            int percentage = (int) (100 * visible / total);
            //LogUtil.i(TAG, view.getTag().toString() + " " + percentage + " % Visible");
            if (percentage >= 50) {
                return percentage;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @JavascriptInterface
    public String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @JavascriptInterface
    public String getDeviceId () {
        if (getFromSharedPrefs("deviceId").equals("__failed")) {
            String dId = GenieService.getService().getDeviceInfo().getDeviceID();
            setInSharedPrefs("deviceId", dId);
            return dId;
        }
        return getFromSharedPrefs("deviceId");
    }

    @JavascriptInterface
    public void displayHTML (final String layoutId, final String sectionName) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String filePath = "";
                int id = parseInt(layoutId);
                switch (sectionName) {
                    case "PRIVACY_POLICY":
                        filePath = "file:///android_asset/privacy_policy.html";
                        break;
                    case "TERMS_OF_SERVICE":
                        filePath = "file:///android_asset/terms_of_service.html";
                        break;
                    case "ABOUT":
                        filePath = "file:///android_asset/about_us.html";
                        break;
                }
                LinearLayout linearLayout = (LinearLayout) activity.findViewById(id);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(16, 8, 16, 8);
                linearLayout.setLayoutParams(layoutParams);
                WebView webview = new WebView(context);
                WebSettings setting = webview.getSettings();
                setting.setMinimumFontSize(16);
                setting.setJavaScriptEnabled(true);
                setting.setLoadWithOverviewMode(true);
//                setting.setUseWideViewPort(true);
                webview.getSettings().setBuiltInZoomControls(true);
                setting.setSupportZoom(true);
                setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
                webview.setWebChromeClient(new WebChromeClient());
                webview.loadUrl(filePath);
                linearLayout.addView(webview);
            }
        });
    }

    @JavascriptInterface
    public void openPlayStoreLink () {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void sendFeedback(String cb, String contentId, String comment, float rating, String pageId, String contentVersion) {
        genieWrapper.sendFeedback(cb, contentId, comment, rating, pageId, contentVersion);
    }

    @JavascriptInterface
    private String getGenieConfigurations() {
        StringBuilder configString = new StringBuilder();

        //add device id
        configString.append("did:");
        configString.append(GenieService.getService().getDeviceInfo().getDeviceID());
        configString.append("||");

        //add device model
        configString.append("mdl:");
        configString.append(DeviceSpec.getDeviceModel());
        configString.append("||");

        //add device make
        configString.append("mak:");
        configString.append(DeviceSpec.getDeviceMaker());
        configString.append("||");

        //add Android OS version
        configString.append("dos:");
        configString.append(DeviceSpec.getOSVersion());
        configString.append("||");

        //add Crosswalk version
        configString.append("cwv:");
        configString.append(getCrossWalkVersion());
        configString.append("||");

        //add Screen Resolution
        configString.append("res:");
        configString.append(DeviceSpec.getScreenWidth(context) + "x" + DeviceSpec.getScreenHeight(context));
        configString.append("||");

        //add Screen DPI
        configString.append("dpi:");
        configString.append(DeviceSpec.getDeviceDensityInDpi(context));
        configString.append("||");

        //add Total disk space
        configString.append("tsp:");
        configString.append(DeviceSpec.getTotalExternalMemorySize() + DeviceSpec.getTotalInternalMemorySize());
        configString.append("||");

        //add free space
        configString.append("fsp:");
        configString.append(DeviceSpec.getAvailableExternalMemorySize() + DeviceSpec.getAvailableInternalMemorySize());
        configString.append("||");

        //add Count of content on device
        configString.append("cno:");
        configString.append(getLocalContentsCount());
        configString.append("||");

        //add total users on device
        configString.append("uno:");
        configString.append(getUsersCount());
        configString.append("||");

        File genieSupportDirectory = FileHandler.getRequiredDirectory(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_PATH);
        String filePath = genieSupportDirectory + "/" + SUNBIRD_SUPPORT_FILE;
        String versionsAndOpenTimes = FileHandler.readFile(filePath);

        //add genie version history
        configString.append("gv:");
        configString.append(versionsAndOpenTimes);
        configString.append("||");

        //add current timestamp
        configString.append("ts:");
        configString.append(System.currentTimeMillis());

        //calculate checksum before adding pipes
        String checksum = Base64Util.encodeToString(CryptoUtil.generateHMAC(configString.toString().trim(),
                GenieService.getService().getDeviceInfo().getDeviceID().getBytes(), JWTokenType.HS256.getAlgorithmName()), 11);
        configString.append("||");

        //add HMAC
        configString.append("csm:");
        configString.append(checksum);

        return configString.toString();
    }

    private String getCrossWalkVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo("org.xwalk.core", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo == null ? "na" : pInfo.versionName;
    }

    private int getLocalContentsCount() {
        ContentFilterCriteria.Builder contentFilterCriteria = new ContentFilterCriteria.Builder();
        contentFilterCriteria.contentTypes(new String[]{ContentType.GAME, ContentType.STORY,
                ContentType.WORKSHEET, ContentType.COLLECTION, ContentType.TEXTBOOK, ContentType.COURSE, ContentType.LESSONPLAN, ContentType.RESOURCE})
                .withContentAccess();
        GenieResponse<List<Content>> genieResponse = GenieService.getService().getContentService().
                getAllLocalContent(contentFilterCriteria.build());
        List<Content> contentList = genieResponse.getResult();
        return contentList == null ? 0 : contentList.size();
    }

    private int getUsersCount() {
        GenieResponse<List<Profile>> response = GenieService.getService().getUserService().getAllUserProfile();
        List<Profile> mUsersList = response.getResult();
        return mUsersList == null ? 0 : mUsersList.size();
    }

    private String SEPERATOR = "~";
    private String SUNBIRD_SUPPORT_FILE = "sunbird_support.txt";

    @JavascriptInterface
    public void makeEntryInSunbirdSupportFile() throws IOException {
        File sunbirdSupportDirectory = FileHandler.getRequiredDirectory(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_PATH);
        String filePath = sunbirdSupportDirectory + "/" + SUNBIRD_SUPPORT_FILE;
//        String packageName = GlobalApplication.getInstance().getClientPackageName();
        String versionName = BuildConfig.VERSION_NAME;

        //for the first time when file does not exists
        if (!FileHandler.checkIfFileExists(filePath)) {
            makeFirstEntryInTheFile(versionName, filePath);
        } else {
            String lastLineOfFile = FileHandler.readLastLineFromFile(filePath);
            if (!StringUtil.isNullOrEmpty(lastLineOfFile)) {
                String[] partsOfLastLine = lastLineOfFile.split(SEPERATOR);

                if (versionName.equalsIgnoreCase(partsOfLastLine[0])) {
                    //just remove the last line from the file and update it their
                    FileHandler.removeLastLineFromFile(filePath);

                    String previousOpenCount = partsOfLastLine[2];
                    int count = Integer.parseInt(previousOpenCount);
                    count++;
                    String updateEntry = versionName + SEPERATOR + partsOfLastLine[1] + SEPERATOR + count;
                    FileHandler.saveToFile(filePath, updateEntry);
                } else {
                    //make a new entry to the file
                    String newEntry = versionName + SEPERATOR + System.currentTimeMillis() + SEPERATOR + "1";
                    FileHandler.saveToFile(filePath, newEntry);
                }
            } else {
                //make a new entry to the file
                String newEntry = versionName + SEPERATOR + System.currentTimeMillis() + SEPERATOR + "1";
                FileHandler.saveToFile(filePath, newEntry);
            }
        }
    }

    private void makeFirstEntryInTheFile(String versionName, String filePath) throws IOException {
        FileHandler.createFileInTheDirectory(filePath);
        String firstEntry = versionName + SEPERATOR + System.currentTimeMillis() + SEPERATOR + "1";
        FileHandler.saveToFile(filePath, firstEntry);
    }

    @JavascriptInterface
    public int getViewHeight(String viewId) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int id = parseInt(viewId);
        View view = activity.findViewById(id);
        if (view != null) return (int) (view.getHeight() / metrics.density);
        return -1;
    }

    @JavascriptInterface
    public int getViewWidth(String viewId) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int id = parseInt(viewId);
        View view = activity.findViewById(id);
        if (view != null) return (int) (view.getWidth() / metrics.density);
        return -1;
    }

    @JavascriptInterface
    public int getScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (int) (metrics.widthPixels / metrics.density);
    }

    @JavascriptInterface
    public int getScreenHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (int) (metrics.heightPixels / metrics.density);
    }

    @JavascriptInterface
    public void scrollTo (String scrollViewId, String scrollX) {
        int id = parseInt(scrollViewId);
        final int x = parseInt(scrollX);
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final HorizontalScrollView scrollView = (HorizontalScrollView) activity.findViewById(id);
        if (scrollView != null) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollBy(10, 0);
//                    scrollView.smoothScrollTo((int) (x * metrics.density),0);
                    ObjectAnimator animator = ObjectAnimator.ofInt(scrollView, "scrollX", (int) (x * metrics.density));
                    animator.setDuration(100);
                    animator.start();
                }
            });
        }
    }

    private int[] cardIds;

    @JavascriptInterface
    public void addScrollListener(final String scrollViewID, final String onStopCb) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int scrollViewId = parseInt(scrollViewID);
                    final HorizontalScrollView scrollView = (HorizontalScrollView) activity.findViewById(scrollViewId);
                    scrollView.smoothScrollBy(0,0);
                    HorizontalScroller scroller = new HorizontalScroller(scrollView);
                    scroller.setOnScrollStoppedListener(new HorizontalScroller.OnScrollStopListener() {
                        @Override
                        public void onScrollStopped(int deltaX) {
                            String javascript = String.format("window.callJSCallback('%s', '%s');", onStopCb, deltaX);
                            dynamicUI.addJsToWebView(javascript);
                        }
                    });
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error in rendering scrollView, err -> ");
                    e.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public void getViews(final String id1) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int scrollViewId = parseInt(id1);
                    final ScrollView scrollView = (ScrollView) activity.findViewById(scrollViewId);
                    scrollView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            switch ( motionEvent.getAction( ) ) {
                                case MotionEvent.ACTION_SCROLL:
                                case MotionEvent.ACTION_MOVE:
                                    //Log.e( "SCROLL", "ACTION_SCROLL" );
                                    break;
                                case MotionEvent.ACTION_DOWN:
                                    //Log.e( "SCROLL", "ACTION_DOWN" );
                                    break;
                                case MotionEvent.ACTION_CANCEL:
                                case MotionEvent.ACTION_UP:
                                    Log.d( "SCROLL", "SCROLL_STOP" );
                                    checkViewVisibility(scrollView);
                                    break;

                            }
                            return false;
                        }
                    });
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error in rendering scrollView, err -> ");
                    e.printStackTrace();
                }
            }
        });
    }
    @JavascriptInterface
    public void setMapId(final String id, final String sectionName, final String sectionId, final String index) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int parentViewId = parseInt(id);
                View view = (View) activity.findViewById(parentViewId);
                String tagArray[] = {sectionName,index,sectionId};
                if(view!=null) {
                    view.setTag(tagArray);
                    mapId.put(id, sectionName);
                }

            }
        });

    }

    @JavascriptInterface
    public void checkViewVisibility(ScrollView scrollView) {
        for (Map.Entry mapItem : mapId.entrySet()) {
            int parentViewId = parseInt(mapItem.getKey().toString());
            final View view =  activity.findViewById(parentViewId);
            if (view == null) continue;
            int percentage = isViewVisible(view, scrollView);
            if (percentage != -1) {
                if (view != null && view.getTag() != null) {
                    String tagArray[] = (String[]) view.getTag();
                    sectionMap.put(tagArray[0], tagArray);
                    //mSectionMapList.add(sectionMap);
                    Log.d("sectionName",tagArray[0]);

                }
            }
        }
    }
    @JavascriptInterface
    public void clearMapId() {
        mapId.clear();
    }

    @JavascriptInterface
    public void logVisitEvent(String type) {
        String pageId = "";

        switch (type) {
            case "HOME":
                pageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                pageId = TelemetryPageId.COURSES;
                break;
            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                break;
            case "GROUPS":
                pageId = TelemetryPageId.GROUPS;
                break;
            case "PROFILE":
                pageId = TelemetryPageId.PROFILE;
                break;
        }

        TelemetryBuilder.buildSectionVisitImpressionEvent(ImpressionType.VIEW,pageId,pageId, ContextEnvironment.HOME, sectionMap);
        sectionMap.clear();
    }

    @JavascriptInterface
    public void logListViewEvent(String type) {
        String pageId = "";

        switch (type) {
            case "HOME":
                pageId = TelemetryPageId.HOME;
                break;
            case "COURSES":
                pageId = TelemetryPageId.COURSES;
                break;
            case "LIBRARY":
                pageId = TelemetryPageId.LIBRARY;
                break;
            case "GROUPS":
                pageId = TelemetryPageId.GROUPS;
                break;
            case "PROFILE":
                pageId = TelemetryPageId.PROFILE;
                break;
        }

        TelemetryBuilder.buildContentVisitImpressionEvent(ImpressionType.VIEW,pageId,pageId, ContextEnvironment.HOME, contentMap);
        contentMap.clear();
    }

    @JavascriptInterface
    public void getFrameworkDetails(String cb) {
        genieWrapper.getFrameworkDetails(cb);
    }

    @JavascriptInterface
    public void animateImageView(String id){
        int id2 = parseInt(id);
        ImageView view = (ImageView) activity.findViewById(id2);
        TranslateAnimation animate = new TranslateAnimation(-50, view.getWidth(), 0, 0);
        animate.setDuration(1000);
        animate.setRepeatCount(Animation.INFINITE);
        animate.setFillAfter(true);

            view.startAnimation(animate);


    }

}