package org.sunbird;

import android.app.Application;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.commons.db.cache.PreferenceWrapper;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.sunbird.telemetry.TelemetryAction;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.utils.ForegroundService;
import org.sunbird.utils.SDKParams;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Vinay on 13/06/17.
 */
public class GlobalApplication extends Application implements ForegroundService.OnForegroundChangeListener {

    private static GlobalApplication instance;
    private static PreferenceWrapper mPreferenceWrapper;

    public static GlobalApplication getInstance() {
        return instance;
    }

    public static PreferenceWrapper getPreferenceWrapper() {
        if (mPreferenceWrapper == null) {
            throw new RuntimeException("Must run initPreferenceWrapper (Application) before instance.");
        }
        return mPreferenceWrapper;
    }

    public void setParams() {
        SDKParams params = new SDKParams();
        String channelId = PreferenceManager.getDefaultSharedPreferences(instance).getString("channelId", "__failed");
        if (StringUtil.isNullOrEmpty(channelId) || channelId.equals("__failed")) {
            channelId = null;
        }
        params.put(SDKParams.Key.CHANNEL_ID, channelId);
        GenieService.setParams(params);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        GenieService.init(this, this.getClass().getPackage().getName());
        initPreferenceWrapper();
        setParams();
        registerActivityLifecycleCallbacks(ForegroundService.getInstance());
        ForegroundService.getInstance().registerListener(this);
        setupCrashlytics();
    }

    private void initPreferenceWrapper() {
        if (mPreferenceWrapper == null) {
            mPreferenceWrapper = new PreferenceWrapper(this, "Sunbird");
        }
    }

    private void setupCrashlytics() {
        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }
    }

    @Override
    public void onSwitchForeground() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInterruptEvent(TelemetryAction.RESUME));
    }

    @Override
    public void onSwitchBackground() {
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildInterruptEvent(TelemetryAction.BACKGROUND));
    }
}
