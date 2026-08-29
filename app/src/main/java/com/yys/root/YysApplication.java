package com.yys.root;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Application class for YYS Assistant.
 * Provides global context and main thread handler.
 */
public class YysApplication extends Application {

    private static YysApplication sInstance;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        ConfigManager.getInstance().init(this);
        TemplateManager.getInstance().init(this);
    }

    public static YysApplication getInstance() {
        return sInstance;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

    public void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mMainHandler.post(runnable);
        }
    }
}
