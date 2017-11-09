package org.sunbird.utils;

import org.ekstep.genieservices.commons.IParams;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JUSPAY\nikith.shetty on 19/9/17.
 */

public class SDKParams implements IParams {

    private Map<String, Object> mValues;

    public SDKParams() {
        this.mValues = new HashMap<>();
    }

    public void put(String key, Object value) {
        this.mValues.put(key, value);
    }

    public String getString(String key) {
        return (String) this.mValues.get(key);
    }

    public long getLong(String key) {
        return ((Long) this.mValues.get(key)).longValue();
    }

    public int getInt(String key) {
        return ((Integer) this.mValues.get(key)).intValue();
    }

    public boolean getBoolean(String key) {
        return ((Boolean) this.mValues.get(key)).booleanValue();
    }

    public boolean contains(String key) {
        return this.mValues.containsKey(key);
    }

    public void remove(String key) {
        this.mValues.remove(key);
    }

    public void clear() {
        this.mValues.clear();
    }
}