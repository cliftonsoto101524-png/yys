package com.yys.root;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

/**
 * Enhanced floating window service for script control.
 * Provides overlay controls for starting/stopping scripts and recording.
 */
public class FloatService extends Service {

    private static final String CHANNEL_ID = "yys_float_service";
    private static final int NOTIFICATION_ID = 1001;

    private WindowManager mWindowManager;
    private View mFloatView;
    private WindowManager.LayoutParams mParams;
    private ScriptEngine mEngine;
    private ScreenshotCollector mCollector;

    private boolean mIsRecording = false;
    private int mInitialX;
    private int mInitialY;
    private float mInitialTouchX;
    private float mInitialTouchY;

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mEngine = new ScriptEngine();
        mCollector = new ScreenshotCollector();
        createNotificationChannel();
        createFloatWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification());
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "YYS Float Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Floating window for YYS Assistant");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("阴阳师辅助 v3.0")
                .setContentText("悬浮窗服务运行中")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createFloatWindow() {
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        int[] pos = ConfigManager.getInstance().getFloatWindowPos();
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.TOP | Gravity.START;
        mParams.x = pos[0];
        mParams.y = pos[1];

        mFloatView = LayoutInflater.from(this).inflate(R.layout.float_window, null);
        setupFloatView();

        try {
            mWindowManager.addView(mFloatView, mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFloatView() {
        final LinearLayout floatRoot = mFloatView.findViewById(R.id.float_root);
        final ImageButton btnToggle = mFloatView.findViewById(R.id.btn_toggle);
        final LinearLayout panelControls = mFloatView.findViewById(R.id.panel_controls);
        final TextView tvStatus = mFloatView.findViewById(R.id.tv_status);

        // Toggle expand/collapse
        btnToggle.setOnClickListener(v -> {
            if (panelControls.getVisibility() == View.VISIBLE) {
                panelControls.setVisibility(View.GONE);
                btnToggle.setImageResource(android.R.drawable.ic_menu_add);
            } else {
                panelControls.setVisibility(View.VISIBLE);
                btnToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            }
        });

        // Drag handling
        btnToggle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mInitialX = mParams.x;
                    mInitialY = mParams.y;
                    mInitialTouchX = event.getRawX();
                    mInitialTouchY = event.getRawY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    mParams.x = mInitialX + (int) (event.getRawX() - mInitialTouchX);
                    mParams.y = mInitialY + (int) (event.getRawY() - mInitialTouchY);
                    mWindowManager.updateViewLayout(mFloatView, mParams);
                    return true;
                case MotionEvent.ACTION_UP:
                    ConfigManager.getInstance().setFloatWindowPos(mParams.x, mParams.y);
                    return false;
            }
            return false;
        });

        // Start/Pause button
        ImageButton btnPlay = mFloatView.findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(v -> {
            if (mEngine.isRunning()) {
                if (mEngine.isPaused()) {
                    mEngine.resume();
                    tvStatus.setText("运行中");
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mEngine.pause();
                    tvStatus.setText("已暂停");
                    btnPlay.setImageResource(android.R.drawable.ic_media_play);
                }
            } else {
                // Start last used script
                String lastScript = ConfigManager.getInstance()
                        .getScriptConfig("last_script").get("name").toString();
                if (YysAuto.hasScript(lastScript)) {
                    startScript(lastScript);
                    tvStatus.setText("运行中");
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    Toast.makeText(this, "请先选择一个脚本", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Stop button
        ImageButton btnStop = mFloatView.findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(v -> {
            mEngine.stop();
            tvStatus.setText("已停止");
            btnPlay.setImageResource(android.R.drawable.ic_media_play);
            Toast.makeText(this, "脚本已停止", Toast.LENGTH_SHORT).show();
        });

        // Screenshot button
        ImageButton btnScreenshot = mFloatView.findViewById(R.id.btn_screenshot);
        btnScreenshot.setOnClickListener(v -> {
            String path = mCollector.takeSingleScreenshot();
            if (path != null) {
                Toast.makeText(this, "截图已保存", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "截图失败", Toast.LENGTH_SHORT).show();
            }
        });

        // Record button
        ImageButton btnRecord = mFloatView.findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(v -> {
            if (mIsRecording) {
                mCollector.stopCollection();
                mIsRecording = false;
                btnRecord.setImageResource(android.R.drawable.ic_menu_camera);
                Toast.makeText(this, "截图收集已停止", Toast.LENGTH_SHORT).show();
            } else {
                mCollector.startCollection();
                mIsRecording = true;
                btnRecord.setImageResource(android.R.drawable.ic_menu_save);
                Toast.makeText(this, "开始自动截图收集", Toast.LENGTH_SHORT).show();
            }
        });

        // Script callback
        mEngine.setCallback(new ScriptEngine.ScriptCallback() {
            @Override
            public void onStart(String scriptName) {
                tvStatus.setText("运行: " + scriptName);
            }

            @Override
            public void onProgress(String scriptName, String status) {
                tvStatus.setText(status);
            }

            @Override
            public void onComplete(String scriptName, boolean success, String message) {
                tvStatus.setText("完成");
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            }

            @Override
            public void onError(String scriptName, String error) {
                tvStatus.setText("错误");
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            }
        });
    }

    private void startScript(String scriptName) {
        YysAuto script = YysAuto.create(scriptName, mEngine);
        if (script != null) {
            mEngine.runScript(scriptName, script::run);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatView != null && mFloatView.isAttachedToWindow()) {
            mWindowManager.removeView(mFloatView);
        }
        if (mEngine != null) {
            mEngine.shutdown();
        }
        if (mCollector != null) {
            mCollector.shutdown();
        }
    }
}
