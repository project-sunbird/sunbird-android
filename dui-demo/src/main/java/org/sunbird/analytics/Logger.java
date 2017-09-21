package org.sunbird.analytics;

import android.support.compat.BuildConfig;
import android.util.Log;

/**
 * Created by sahebjot on 31/03/17.
 */
public class Logger {

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable error) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message + " - " + error.getLocalizedMessage());
        }
    }

    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message);
        }
    }

    public static void exception(Throwable exception) {
        if (BuildConfig.DEBUG) {
            Log.e("exception", exception.getLocalizedMessage());
        }
    }
}
