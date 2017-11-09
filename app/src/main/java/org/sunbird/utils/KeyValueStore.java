package org.sunbird.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by juspay on 4/18/17.
 */

public class KeyValueStore {
    private final SharedPreferences preferences;

    public KeyValueStore(Context context) {
        this.preferences = context.getSharedPreferences("SUNBIRDPreferences", Activity.MODE_PRIVATE);
    }

    public void write(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void write(String key, Long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public boolean contains(String key) {
        return this.preferences.contains(key);
    }

    public void write(String key, Boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void write(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public Boolean getBoolean(String key, boolean value) {
        return this.preferences.getBoolean(key, value);
    }

    public long getLong(String key, long value) {
        return this.preferences.getLong(key, value);
    }

    public String getString(String key, String value) {
        return this.preferences.getString(key, value);
    }

    public int getInt(String key, int value) {
        return this.preferences.getInt(key, value);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public void clearAllPreferences(Context context) {
        SharedPreferences settings = context.getSharedPreferences("SUNBIRDPreferences", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }
}
