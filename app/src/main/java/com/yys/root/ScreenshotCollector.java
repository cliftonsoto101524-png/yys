package com.yys.root;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Automatic screenshot collector.
 * Captures screenshots at intervals and saves them for template creation.
 */
public class ScreenshotCollector {

    private static final String TAG = "ScreenshotCollector";
    private static final String COLLECTOR_DIR = "YysScreenshots";

    private final RootShell mShell;
    private final ExecutorService mExecutor;
    private final Handler mMainHandler;
    private final File mCollectorDir;

    private boolean mIsCollecting = false;
    private int mIntervalMs = 2000;
    private int mMaxScreenshots = 100;
    private int mScreenshotCount = 0;
    private OnScreenshotListener mListener;

    public interface OnScreenshotListener {
        void onScreenshotTaken(String path, int count);
        void onCollectionComplete(int total);
        void onError(String message);
    }

    public ScreenshotCollector() {
        mShell = RootShell.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
        mCollectorDir = new File(Environment.getExternalStorageDirectory(), COLLECTOR_DIR);
        if (!mCollectorDir.exists()) {
            mCollectorDir.mkdirs();
        }
    }

    public void setListener(OnScreenshotListener listener) {
        mListener = listener;
    }

    public void setInterval(int intervalMs) {
        mIntervalMs = Math.max(500, intervalMs);
    }

    public void setMaxScreenshots(int max) {
        mMaxScreenshots = Math.max(1, max);
    }

    public void startCollection() {
        if (mIsCollecting) return;
        mIsCollecting = true;
        mScreenshotCount = 0;

        mExecutor.execute(() -> {
            while (mIsCollecting && mScreenshotCount < mMaxScreenshots) {
                takeScreenshot();
                try {
                    Thread.sleep(mIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            mIsCollecting = false;
            if (mListener != null) {
                mMainHandler.post(() -> mListener.onCollectionComplete(mScreenshotCount));
            }
        });
    }

    public void stopCollection() {
        mIsCollecting = false;
    }

    public boolean isCollecting() {
        return mIsCollecting;
    }

    public int getScreenshotCount() {
        return mScreenshotCount;
    }

    private void takeScreenshot() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())
                .format(new Date());
        String filename = "screenshot_" + timestamp + ".png";
        File file = new File(mCollectorDir, filename);

        boolean success = mShell.screenshot(file.getAbsolutePath());
        if (success) {
            mScreenshotCount++;
            if (mListener != null) {
                mMainHandler.post(() -> mListener.onScreenshotTaken(file.getAbsolutePath(), mScreenshotCount));
            }
        } else {
            Log.e(TAG, "Screenshot failed: " + filename);
            if (mListener != null) {
                mMainHandler.post(() -> mListener.onError("Screenshot capture failed"));
            }
        }
    }

    /**
     * Take a single screenshot and return the path.
     */
    public String takeSingleScreenshot() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())
                .format(new Date());
        String filename = "screenshot_" + timestamp + ".png";
        File file = new File(mCollectorDir, filename);

        if (mShell.screenshot(file.getAbsolutePath())) {
            return file.getAbsolutePath();
        }
        return null;
    }

    /**
     * Take a screenshot and load it as Bitmap.
     */
    public Bitmap takeScreenshotBitmap() {
        String path = takeSingleScreenshot();
        if (path != null) {
            return BitmapFactory.decodeFile(path);
        }
        return null;
    }

    /**
     * Save a bitmap to the collector directory.
     */
    public String saveBitmap(Bitmap bitmap, String name) {
        if (bitmap == null || bitmap.isRecycled()) return null;
        File file = new File(mCollectorDir, name + ".png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap", e);
            return null;
        }
    }

    /**
     * Get all collected screenshots.
     */
    public File[] getCollectedScreenshots() {
        return mCollectorDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
    }

    /**
     * Clear all collected screenshots.
     */
    public void clearScreenshots() {
        File[] files = getCollectedScreenshots();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        mScreenshotCount = 0;
    }

    public void shutdown() {
        stopCollection();
        mExecutor.shutdown();
    }
}
