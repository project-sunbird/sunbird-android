package org.sunbird.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.ekstep.genieservices.commons.bean.enums.InteractionType;
import org.ekstep.genieservices.commons.utils.Base64Util;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.sunbird.GlobalApplication;
import org.sunbird.R;
import org.sunbird.core.JsInterface;
import org.sunbird.models.Notification;
import org.sunbird.notification.enums.NotificationActionId;
import org.sunbird.telemetry.TelemetryAction;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryConstant;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.telemetry.TelemetryStageId;
import org.sunbird.utils.Constants;
import org.sunbird.utils.GenieWrapper;
import org.sunbird.utils.ImagePicker;
import org.sunbird.utils.NewLogger;
import org.sunbird.utils.PreferenceKey;
import org.sunbird.utils.WebSocket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.juspay.mystique.DynamicUI;
import in.juspay.mystique.ErrorCallback;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    public final static int IMAGE_CHOOSER_ID = 865;
    private static final String TAG = "MainActivity";
    private static final int SEND_SMS_REQUEST = 8;
    private static CustomTabsClient mClient;
    private static CustomTabsServiceConnection mConnection;
    public int PERMISSION_CODE = 111;
    protected GenieWrapper genieWrapper;
    private DynamicUI dynamicUI;
    private JsInterface jsInterface;
    private String chromePackageName;

    public static String getContentName(ContentResolver resolver, Uri uri) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
        if (nameIndex >= 0) {
            return cursor.getString(nameIndex);
        } else {
            return null;
        }
    }

    public static void showAppUpdateDialog() {

    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        genieWrapper = new GenieWrapper(MainActivity.this, dynamicUI);
        GlobalApplication.getPreferenceWrapper().putLong(PreferenceKey.APPLICATION_START_TIME, System.currentTimeMillis());

        askPermissions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        setContentView(R.layout.activity_main);

        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGenieStartEvent(MainActivity.this));

        FrameLayout container = (FrameLayout) findViewById(R.id.dui_container);
        dynamicUI = new DynamicUI(this, container, null, new ErrorCallback() {
            @Override
            public void onError(String errorType, String errorMessage) {
            }
        });

        WebSocket ws = new WebSocket(this);
        jsInterface = new JsInterface(this, dynamicUI, ws);

        DynamicUI.setLogger(new NewLogger());
        dynamicUI.loadURL(getResources().getString(R.string.index_base_url));
        dynamicUI.addJavascriptInterface(jsInterface, "JBridge");

//        RestClient.init(this.getApplicationContext());

        this.mConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mClient = customTabsClient;
                mClient.warmup(0);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mClient = null;
            }
        };

        chromePackageName = "com.android.chrome";
        CustomTabsClient.bindCustomTabsService(this, chromePackageName, this.mConnection);

        Intent intent = getIntent();
        String linkFromIntent = "";
        String fileFromIntent = "";

        if (intent != null) {
            if (intent.getExtras() != null && intent.getExtras().containsKey(Constants.BUNDLE_KEY_NOTIFICATION_DATA_MODEL)) {
                Notification notification = (Notification) intent.getExtras().getSerializable(Constants.BUNDLE_KEY_NOTIFICATION_DATA_MODEL);
                if (notification != null) {
                    Map<String, Object> valuesMap = new HashMap<>();
                    valuesMap.put(TelemetryConstant.NOTIFICATION_DATA, GsonUtil.getGson().toJson(notification));
                    TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH, TelemetryStageId.SERVER_NOTIFICATION, TelemetryAction.NOTIFICATION_CLICKED, null, valuesMap));
                    switch (notification.getActionid()) {
                        case NotificationActionId.ANNOUNCEMENT_DETAIL:
                            jsInterface.setInSharedPrefs("screenToOpen", "ANNOUNCEMENT_DETAIL");
                            break;
                        case NotificationActionId.ANNOUNCEMENT_LIST:
                            jsInterface.setInSharedPrefs("screenToOpen", "ANNOUNCEMENT_LIST");
                            break;
                        case NotificationActionId.DO_NOTHING:
                        default:
                            jsInterface.setInSharedPrefs("screenToOpen", "DO_NOTHING");
                            break;
                    }
                    jsInterface.setInSharedPrefs("intentNotification", Base64Util.encodeToString(GsonUtil.toJson(notification).getBytes(), Base64Util.DEFAULT));
                    Log.d(TAG, "Intent from notification");
                }
            }
            if (intent.getData() != null) {

                if (intent.getScheme().equals("http") || intent.getScheme().equals("https")) {
                    linkFromIntent = intent.getDataString();
                    jsInterface.setInSharedPrefs("intentLinkPath", linkFromIntent);
                    Log.d("Link shared from intent", linkFromIntent);

                } else if (intent.getScheme().equals("file")) {
                    fileFromIntent = getIntent().getData().getPath();
                    jsInterface.setInSharedPrefs("intentFilePath", fileFromIntent);
                    Log.d("File shared from intent", fileFromIntent);
                } else if (intent.getScheme().equals("content")) {

                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                        Uri uri = getIntent().getData();
                        String name = getContentName(getContentResolver(), uri);
                        try {
                            InputStream attachment = getContentResolver().openInputStream(getIntent().getData());
                            if (attachment == null)
                                Log.e("onCreate", "cannot access mail attachment");
                            else {

                                File directory = new File(Environment.getExternalStorageDirectory() + File.separator + ".GmailSunbird");

                                if (!directory.exists()) {
                                    directory.mkdirs();
                                }

                                File ecarFile = new File(directory.getAbsolutePath() + File.separator + name);
                                if (!ecarFile.exists()) {
                                    try {
                                        ecarFile.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                FileOutputStream tmp = new FileOutputStream(ecarFile);
                                byte[] buffer = new byte[1024];
                                while (attachment.read(buffer) > 0)
                                    tmp.write(buffer);

                                tmp.close();
                                attachment.close();
                                fileFromIntent = ecarFile.getAbsolutePath();
                                jsInterface.setInSharedPrefs("intentFilePath", fileFromIntent);
                                Log.d("File shared from intent", fileFromIntent);

                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        dynamicUI.addJsToWebView("window.onBackPressed()");
    }

    protected void onDestroy() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGenieEndEvent());
        dynamicUI.addJsToWebView("window.onDestroy()");
        System.exit(0);
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        dynamicUI.addJsToWebView("window.onStop()");
        super.onStop();
    }

    @Override
    protected void onPause() {
        dynamicUI.addJsToWebView("window.onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        dynamicUI.addJsToWebView("window.onResume()");
        Log.d("test!", "asd");
        super.onResume();
    }

    public CustomTabsClient getCustomTabsClient() {
        return mClient;
    }

    public void onWebSocketCallback(String message) {
        String base64Data = Base64.encodeToString(message.getBytes(), Base64.NO_WRAP);
        String javascript = String.format("window.onWebSocketMessage('%s');", base64Data);
        dynamicUI.addJsToWebView(javascript);
    }

    public void askPermissions() {

        ArrayList<String> al = new ArrayList<String>();
        String[] allPermissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (int i = 0; i < allPermissions.length; i++) {
            if (!(ContextCompat.checkSelfPermission(this, allPermissions[i]) == PackageManager.PERMISSION_GRANTED)) {
                al.add(allPermissions[i]);
            }
        }

        String[] backUpArray = new String[al.size()];
        String[] ungrantedPermissions = al.toArray(backUpArray);

        if (ungrantedPermissions.length != 0 && ungrantedPermissions != null) {
            ActivityCompat.requestPermissions(this, ungrantedPermissions, PERMISSION_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int i = 0;
        if (requestCode == PERMISSION_CODE) {
            for (i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    break;
                }
            }

            if (i == grantResults.length) {
                Log.d("TELEMETRY IN ONCREATE", "STARTED TELEMETRY");
            } else {
                Log.d("PERMISSION DENIED", "PERMISSION DENIED");
            }

        } else {
            jsInterface.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if (requestCode == IMAGE_CHOOSER_ID) {
                String picturePath = getPath(ImagePicker.getImageUriFromResult(this, resultCode, intent));
                Log.e(TAG, "onActivityResult: " + picturePath);
                String javascript = String.format("window.onGetImageFromGallery('%s')", picturePath);
                dynamicUI.addJsToWebView(javascript);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Exception in onActivity Result", e);
        }
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "Activity Result is " + requestCode);
    }

    private String getPath(Uri uri) {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor;
            try {
                cursor = this.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

}
