package com.yys.root;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced configuration manager using SharedPreferences.
 * Stores script settings, UI preferences, and runtime configs.
 */
public class ConfigManager {

    private static final String PREFS_NAME = "yys_config_v3";
    private static final String KEY_SCRIPT_CONFIGS = "script_configs";
    private static final String KEY_CLOUD_VERSION = "cloud_version";
    private static final String KEY_LAST_CHECK_TIME = "last_check_time";
    private static final String KEY_AUTO_START = "auto_start";
    private static final String KEY_FLOAT_WINDOW_POS = "float_window_pos";
    private static final String KEY_MATCH_THRESHOLD = "match_threshold";
    private static final String KEY_CLICK_DELAY = "click_delay";
    private static final String KEY_SWIPE_DURATION = "swipe_duration";
    private static final String KEY_SCREENSHOT_QUALITY = "screenshot_quality";
    private static final String KEY_ANTI_DETECTION = "anti_detection_enabled";
    private static final String KEY_AUTO_RECONNECT = "auto_reconnect";
    private static final String KEY_RECONNECT_TIMEOUT = "reconnect_timeout";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_TEMPLATE_VERSION = "template_version";
    private static final String KEY_SCRIPT_ENABLED_PREFIX = "script_enabled_";
    private static final String KEY_CLOUD_UPDATE_URL = "cloud_update_url";

    private static ConfigManager sInstance;
    private SharedPreferences mPrefs;
    private final Gson mGson = new Gson();

    private ConfigManager() {}

    public static synchronized ConfigManager getInstance() {
        if (sInstance == null) {
            sInstance = new ConfigManager();
        }
        return sInstance;
    }

    void init(Context context) {
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!mPrefs.contains(KEY_DEVICE_ID)) {
            setDeviceId(java.util.UUID.randomUUID().toString());
        }
        if (!mPrefs.contains(KEY_MATCH_THRESHOLD)) {
            setMatchThreshold(0.85f);
        }
        if (!mPrefs.contains(KEY_CLICK_DELAY)) {
            setClickDelay(500);
        }
        if (!mPrefs.contains(KEY_SWIPE_DURATION)) {
            setSwipeDuration(300);
        }
        if (!mPrefs.contains(KEY_SCREENSHOT_QUALITY)) {
            setScreenshotQuality(80);
        }
        if (!mPrefs.contains(KEY_RECONNECT_TIMEOUT)) {
            setReconnectTimeout(30000);
        }
        if (!mPrefs.contains(KEY_CLOUD_UPDATE_URL)) {
            setCloudUpdateUrl("https://yys-update.example.com/api/v1");
        }
    }

    // Script configs as JSON map
    public Map<String, Object> getScriptConfig(String scriptName) {
        String json = mPrefs.getString(KEY_SCRIPT_CONFIGS, "{}");
        Map<String, Map<String, Object>> all = mGson.fromJson(json,
                new TypeToken<Map<String, Map<String, Object>>>(){}.getType());
        if (all == null) all = new HashMap<>();
        Map<String, Object> cfg = all.get(scriptName);
        return cfg != null ? cfg : new HashMap<>();
    }

    public void setScriptConfig(String scriptName, Map<String, Object> config) {
        String json = mPrefs.getString(KEY_SCRIPT_CONFIGS, "{}");
        Map<String, Map<String, Object>> all = mGson.fromJson(json,
                new TypeToken<Map<String, Map<String, Object>>>(){}.getType());
        if (all == null) all = new HashMap<>();
        all.put(scriptName, config);
        mPrefs.edit().putString(KEY_SCRIPT_CONFIGS, mGson.toJson(all)).apply();
    }

    public boolean isScriptEnabled(String scriptName) {
        return mPrefs.getBoolean(KEY_SCRIPT_ENABLED_PREFIX + scriptName, false);
    }

    public void setScriptEnabled(String scriptName, boolean enabled) {
        mPrefs.edit().putBoolean(KEY_SCRIPT_ENABLED_PREFIX + scriptName, enabled).apply();
    }

    public String getCloudVersion() {
        return mPrefs.getString(KEY_CLOUD_VERSION, "3.0.0");
    }

    public void setCloudVersion(String version) {
        mPrefs.edit().putString(KEY_CLOUD_VERSION, version).apply();
    }

    public long getLastCheckTime() {
        return mPrefs.getLong(KEY_LAST_CHECK_TIME, 0);
    }

    public void setLastCheckTime(long time) {
        mPrefs.edit().putLong(KEY_LAST_CHECK_TIME, time).apply();
    }

    public boolean isAutoStart() {
        return mPrefs.getBoolean(KEY_AUTO_START, false);
    }

    public void setAutoStart(boolean autoStart) {
        mPrefs.edit().putBoolean(KEY_AUTO_START, autoStart).apply();
    }

    public int[] getFloatWindowPos() {
        String str = mPrefs.getString(KEY_FLOAT_WINDOW_POS, "100,200");
        String[] parts = str.split(",");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    public void setFloatWindowPos(int x, int y) {
        mPrefs.edit().putString(KEY_FLOAT_WINDOW_POS, x + "," + y).apply();
    }

    public float getMatchThreshold() {
        return mPrefs.getFloat(KEY_MATCH_THRESHOLD, 0.85f);
    }

    public void setMatchThreshold(float threshold) {
        mPrefs.edit().putFloat(KEY_MATCH_THRESHOLD, threshold).apply();
    }

    public int getClickDelay() {
        return mPrefs.getInt(KEY_CLICK_DELAY, 500);
    }

    public void setClickDelay(int delay) {
        mPrefs.edit().putInt(KEY_CLICK_DELAY, delay).apply();
    }

    public int getSwipeDuration() {
        return mPrefs.getInt(KEY_SWIPE_DURATION, 300);
    }

    public void setSwipeDuration(int duration) {
        mPrefs.edit().putInt(KEY_SWIPE_DURATION, duration).apply();
    }

    public int getScreenshotQuality() {
        return mPrefs.getInt(KEY_SCREENSHOT_QUALITY, 80);
    }

    public void setScreenshotQuality(int quality) {
        mPrefs.edit().putInt(KEY_SCREENSHOT_QUALITY, quality).apply();
    }

    public boolean isAntiDetectionEnabled() {
        return mPrefs.getBoolean(KEY_ANTI_DETECTION, true);
    }

    public void setAntiDetectionEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_ANTI_DETECTION, enabled).apply();
    }

    public boolean isAutoReconnect() {
        return mPrefs.getBoolean(KEY_AUTO_RECONNECT, true);
    }

    public void setAutoReconnect(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_AUTO_RECONNECT, enabled).apply();
    }

    public int getReconnectTimeout() {
        return mPrefs.getInt(KEY_RECONNECT_TIMEOUT, 30000);
    }

    public void setReconnectTimeout(int timeout) {
        mPrefs.edit().putInt(KEY_RECONNECT_TIMEOUT, timeout).apply();
    }

    public String getDeviceId() {
        return mPrefs.getString(KEY_DEVICE_ID, "");
    }

    void setDeviceId(String id) {
        mPrefs.edit().putString(KEY_DEVICE_ID, id).apply();
    }

    public int getTemplateVersion() {
        return mPrefs.getInt(KEY_TEMPLATE_VERSION, 1);
    }

    public void setTemplateVersion(int version) {
        mPrefs.edit().putInt(KEY_TEMPLATE_VERSION, version).apply();
    }

    public String getCloudUpdateUrl() {
        return mPrefs.getString(KEY_CLOUD_UPDATE_URL, "");
    }

    public void setCloudUpdateUrl(String url) {
        mPrefs.edit().putString(KEY_CLOUD_UPDATE_URL, url).apply();
    }

    public void resetToDefaults() {
        mPrefs.edit().clear().apply();
        init(YysApplication.getAppContext());
    }
}
