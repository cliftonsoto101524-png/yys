package com.yys.root;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/**
 * Background service for running scripts without float window.
 */
public class ScriptRunnerService extends Service {

    private static final String CHANNEL_ID = "yys_script_runner";
    private static final int NOTIFICATION_ID = 1002;

    private ScriptEngine mEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        mEngine = new ScriptEngine();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification("脚本服务启动中"));

        if (intent != null) {
            String scriptName = intent.getStringExtra("script_name");
            if (scriptName != null && YysAuto.hasScript(scriptName)) {
                startScript(scriptName);
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "YYS Script Runner",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Background script execution");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification(String text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("阴阳师辅助 - 脚本运行中")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void startScript(String scriptName) {
        YysAuto script = YysAuto.create(scriptName, mEngine);
        if (script == null) return;

        mEngine.setCallback(new ScriptEngine.ScriptCallback() {
            @Override
            public void onStart(String name) {
                updateNotification("运行: " + name);
            }

            @Override
            public void onProgress(String name, String status) {
                updateNotification(status);
            }

            @Override
            public void onComplete(String name, boolean success, String message) {
                updateNotification("完成: " + name);
                stopSelf();
            }

            @Override
            public void onError(String name, String error) {
                updateNotification("错误: " + error);
                stopSelf();
            }
        });

        mEngine.runScript(scriptName, script::run);
    }

    private void updateNotification(String text) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(text));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEngine != null) {
            mEngine.shutdown();
        }
    }
}
