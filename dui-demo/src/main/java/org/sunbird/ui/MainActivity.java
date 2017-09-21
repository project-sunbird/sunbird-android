package org.sunbird.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import org.sunbird.GlobalApplication;
import org.sunbird.R;
import org.sunbird.core.JsInterface;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.utils.GenieWrapper;
import org.sunbird.utils.NewLogger;
import org.sunbird.utils.PreferenceKey;
import org.sunbird.utils.WebSocket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import in.juspay.mystique.DynamicUI;
import in.juspay.mystique.ErrorCallback;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

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

}
