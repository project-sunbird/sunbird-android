package org.sunbird.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.ekstep.genieservices.commons.IPlayerConfig;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.utils.ReflectionUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.content.ContentConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 7/17/2017.
 *
 * @author anil
 */
public class PlayerConfig implements IPlayerConfig {

    private static final String TAG = PlayerConfig.class.getSimpleName();

    private static final String GENIE_CANVAS_PACKAGE = "org.ekstep.geniecanvas";
    private static final String GENIE_QUIZ_APP_PACKAGE = "org.ekstep.quiz.app";
    private static final String GENIE_CANVAS_ACTIVITY = "org.ekstep.geniecanvas.MainActivity";

    @Override
    public Intent getPlayerIntent(Context context, Content content) {
        PackageManager manager = context.getPackageManager();
        String osId = content.getContentData().getOsId();

        Intent intent;
        if (isApk(content.getMimeType())) {
            if (isAppInstalled(context, osId)) {
                intent = manager.getLaunchIntentForPackage(osId);
            } else {
                openPlaystore(context, osId);
                return null;
            }

        } else if (isSupportedFormat(content.getMimeType())) {
            if (osId == null || GENIE_QUIZ_APP_PACKAGE.equals(osId) || GENIE_CANVAS_PACKAGE.equals(osId)) {
                Class<?> className = ReflectionUtil.getClass(GENIE_CANVAS_ACTIVITY);
                if (className == null) {
                    Toast.makeText(context, "Content player not found", Toast.LENGTH_SHORT).show();
                    return null;
                }
                intent = new Intent(context, className);

                HashMap<String, Object> plugins = new HashMap<>();
                plugins.put("id", "org.sunbird.player.endpage");
                plugins.put("ver", "1.0");
                plugins.put("type", "plugin");

                List<Map> pluginsList = new ArrayList<>();
                pluginsList.add(plugins);


                HashMap<String, Object> splashMap = new HashMap<>();
                splashMap.put("text","");
                splashMap.put("icon","");
                splashMap.put("bgImage","");
                splashMap.put("webLink","");

                HashMap<String, Object> config = new HashMap<>();
                config.put("showEndPage", false);
                config.put("plugins", pluginsList);
                config.put("splash", splashMap);
                intent.putExtra("config", config);

            } else {
                Toast.makeText(context, "Content player not found", Toast.LENGTH_SHORT).show();
                return null;
            }
        } else {
            Toast.makeText(context, "Content type not supported", Toast.LENGTH_SHORT).show();
            return null;
        }

        return intent;
    }

    private boolean isSupportedFormat(String mimeType) {
        Log.d("IS SUPPORTED FORMAT", mimeType);
        return ContentConstants.MimeType.ECML.equals(mimeType)
                || ContentConstants.MimeType.HTML.equals(mimeType)
                || ("application/pdf").equals(mimeType)
                || ("video/mp4").equals(mimeType)
                || ("video/x-youtube").equals(mimeType)
                || ("application/vnd.ekstep.h5p-archive").equals(mimeType)
                || ("video/webm").equals(mimeType)
                || ("application/epub").equals(mimeType);


    }

    private boolean isApk(String mimeType) {
        return ContentConstants.MimeType.APK.equals(mimeType);
    }

    private boolean isAppInstalled(Context context, String packageName) {
        if (StringUtil.isNullOrEmpty(packageName)) {
            return false;
        }

        boolean isInstalled;
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            isInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage(), e);
            isInstalled = false;
        }

        return isInstalled;
    }

    private void openPlaystore(Context context, String osId) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.android.vending");
        ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.activities.LaunchUrlHandlerActivity");
        launchIntent.setComponent(comp);
        launchIntent.setData(Uri.parse("market://details?id=" + osId));
        context.startActivity(launchIntent);
    }
}
