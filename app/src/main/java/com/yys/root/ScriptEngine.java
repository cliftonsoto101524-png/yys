package com.yys.root;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.opencv.core.Point;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced script engine supporting multiple operation types.
 * Provides high-level automation primitives for game scripts.
 */
public class ScriptEngine {

    private static final String TAG = "ScriptEngine";

    public interface ScriptCallback {
        void onStart(String scriptName);
        void onProgress(String scriptName, String status);
        void onComplete(String scriptName, boolean success, String message);
        void onError(String scriptName, String error);
    }

    public final RootShell mShell;
    private final ImageMatcher mMatcher;
    private final AntiDetection mAntiDetection;
    private final TemplateManager mTemplateManager;
    private final ExecutorService mExecutor;
    private final Handler mMainHandler;

    private boolean mRunning = false;
    private boolean mPaused = false;
    private String mCurrentScript = null;
    private ScriptCallback mCallback = null;
    public int[] mScreenSize = new int[]{1080, 1920};

    public ScriptEngine() {
        mShell = RootShell.getInstance();
        mMatcher = new ImageMatcher();
        mAntiDetection = new AntiDetection();
        mTemplateManager = TemplateManager.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void setCallback(ScriptCallback callback) {
        mCallback = callback;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public String getCurrentScript() {
        return mCurrentScript;
    }

    public void setScreenSize(int w, int h) {
        mScreenSize = new int[]{w, h};
        mMatcher.setScreenSize(w, h);
    }

    /**
     * Run a script on background thread.
     */
    public void runScript(final String scriptName, final Runnable scriptRunnable) {
        if (mRunning) {
            Log.w(TAG, "Script already running: " + mCurrentScript);
            return;
        }
        mRunning = true;
        mPaused = false;
        mCurrentScript = scriptName;

        notifyStart(scriptName);

        mExecutor.execute(() -> {
            try {
                // Ensure screen size is known
                if (mScreenSize[0] == 1080 && mScreenSize[1] == 1920) {
                    int[] size = mShell.getScreenSize();
                    setScreenSize(size[0], size[1]);
                }

                scriptRunnable.run();
                notifyComplete(scriptName, true, "Completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Script error: " + scriptName, e);
                notifyError(scriptName, e.getMessage());
                notifyComplete(scriptName, false, e.getMessage());
            } finally {
                mRunning = false;
                mCurrentScript = null;
            }
        });
    }

    /**
     * Stop current script.
     */
    public void stop() {
        mRunning = false;
        mPaused = false;
    }

    /**
     * Pause current script.
     */
    public void pause() {
        mPaused = true;
    }

    /**
     * Resume current script.
     */
    public void resume() {
        mPaused = false;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Check if should pause/sleep.
     */
    public void checkPause() {
        if (!mRunning) {
            throw new ScriptStopException("Script stopped");
        }
        while (mPaused && mRunning) {
            try {
                synchronized (this) {
                    wait(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (!mRunning) {
            throw new ScriptStopException("Script stopped");
        }
    }

    /**
     * Sleep with pause support.
     */
    public void sleep(long ms) {
        checkPause();
        long remaining = ms;
        while (remaining > 0 && mRunning && !mPaused) {
            long chunk = Math.min(remaining, 200);
            try {
                Thread.sleep(chunk);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            remaining -= chunk;
            checkPause();
        }
    }

    // --- Image Matching Primitives ---

    /**
     * Take screenshot and match template.
     */
    public ImageMatcher.MatchResult findTemplate(String module, String templateName) {
        String path = mTemplateManager.getTemplatePath(module, templateName);
        if (path == null) {
            Log.w(TAG, "Template not found: " + module + "/" + templateName);
            return new ImageMatcher.MatchResult(false, 0, 0, 0, 0, 0);
        }
        Bitmap screenshot = takeScreenshot();
        if (screenshot == null) return new ImageMatcher.MatchResult(false, 0, 0, 0, 0, 0);

        ImageMatcher.MatchResult result = mMatcher.matchMultiScale(screenshot, path);
        screenshot.recycle();
        return result;
    }

    /**
     * Wait for template to appear on screen.
     */
    public ImageMatcher.MatchResult waitForTemplate(String module, String templateName, int timeoutMs, int checkIntervalMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs && mRunning) {
            checkPause();
            ImageMatcher.MatchResult result = findTemplate(module, templateName);
            if (result.found) {
                return result;
            }
            sleep(checkIntervalMs);
        }
        return new ImageMatcher.MatchResult(false, 0, 0, 0, 0, 0);
    }

    /**
     * Wait for and click a template.
     */
    public boolean waitAndClick(String module, String templateName, int timeoutMs) {
        ImageMatcher.MatchResult result = waitForTemplate(module, templateName, timeoutMs, 1000);
        if (result.found) {
            clickAt(result.centerX(), result.centerY());
            return true;
        }
        return false;
    }

    /**
     * Click if template is present.
     */
    public boolean clickIfPresent(String module, String templateName) {
        ImageMatcher.MatchResult result = findTemplate(module, templateName);
        if (result.found) {
            clickAt(result.centerX(), result.centerY());
            return true;
        }
        return false;
    }

    /**
     * Click at screen coordinates.
     */
    public void clickAt(int x, int y) {
        checkPause();
        int[] coords = mAntiDetection.randomizeCoordinates(x, y, 5);
        mShell.tap(coords[0], coords[1]);
    }

    /**
     * Swipe on screen.
     */
    public void swipe(int x1, int y1, int x2, int y2) {
        checkPause();
        int[] c1 = mAntiDetection.randomizeCoordinates(x1, y1, 3);
        int[] c2 = mAntiDetection.randomizeCoordinates(x2, y2, 3);
        mShell.swipe(c1[0], c1[1], c2[0], c2[1]);
    }

    /**
     * Long press at coordinates.
     */
    public void longPress(int x, int y, int durationMs) {
        checkPause();
        int[] coords = mAntiDetection.randomizeCoordinates(x, y, 5);
        mShell.longPress(coords[0], coords[1], durationMs);
    }

    /**
     * Input text.
     */
    public void inputText(String text) {
        checkPause();
        mShell.inputText(text);
    }

    /**
     * Send key event.
     */
    public void sendKey(int keyCode) {
        checkPause();
        mShell.sendKey(keyCode);
    }

    /**
     * Take screenshot and return bitmap.
     */
    public Bitmap takeScreenshot() {
        String path = "/sdcard/yys_temp_screenshot.png";
        if (mShell.screenshot(path)) {
            return BitmapFactory.decodeFile(path);
        }
        return null;
    }

    /**
     * Check if game is running.
     */
    public boolean isGameRunning() {
        return mShell.isPackageRunning("com.netease.onmyoji");
    }

    /**
     * Start the game.
     */
    public void startGame() {
        mShell.startActivity("com.netease.onmyoji", "com.netease.onmyoji.Launcher");
    }

    /**
     * Check if any of the templates are present.
     */
    public boolean anyTemplatePresent(String module, String... templateNames) {
        Bitmap screenshot = takeScreenshot();
        if (screenshot == null) return false;

        try {
            for (String name : templateNames) {
                String path = mTemplateManager.getTemplatePath(module, name);
                if (path != null) {
                    ImageMatcher.MatchResult result = mMatcher.match(screenshot, path);
                    if (result.found) return true;
                }
            }
        } finally {
            screenshot.recycle();
        }
        return false;
    }

    /**
     * Wait for any of the templates to appear.
     */
    public ImageMatcher.MatchResult waitForAnyTemplate(String module, int timeoutMs, String... templateNames) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs && mRunning) {
            checkPause();
            Bitmap screenshot = takeScreenshot();
            if (screenshot == null) {
                sleep(500);
                continue;
            }
            try {
                for (String name : templateNames) {
                    String path = mTemplateManager.getTemplatePath(module, name);
                    if (path != null) {
                        ImageMatcher.MatchResult result = mMatcher.match(screenshot, path);
                        if (result.found) return result;
                    }
                }
            } finally {
                screenshot.recycle();
            }
            sleep(800);
        }
        return new ImageMatcher.MatchResult(false, 0, 0, 0, 0, 0);
    }

    /**
     * Check for stuck/reconnect screen and handle it.
     */
    public boolean handleReconnect() {
        if (!ConfigManager.getInstance().isAutoReconnect()) return false;
        // Check for common reconnect/stuck indicators
        ImageMatcher.MatchResult result = findTemplate("common", "reconnect_button");
        if (result.found) {
            clickAt(result.centerX(), result.centerY());
            sleep(ConfigManager.getInstance().getReconnectTimeout());
            return true;
        }
        return false;
    }

    /**
     * Find color on screen.
     */
    public Point findColor(int color, int tolerance) {
        Bitmap screenshot = takeScreenshot();
        if (screenshot == null) return null;
        Point p = mMatcher.findColor(screenshot, color, tolerance);
        screenshot.recycle();
        return p;
    }

    /**
     * Wait for color.
     */
    public Point waitForColor(int color, int tolerance, int timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs && mRunning) {
            checkPause();
            Point p = findColor(color, tolerance);
            if (p != null) return p;
            sleep(500);
        }
        return null;
    }

    /**
     * Perform a loop action N times.
     */
    public void loop(int times, LoopAction action) {
        for (int i = 0; i < times && mRunning; i++) {
            checkPause();
            action.run(i);
        }
    }

    public interface LoopAction {
        void run(int iteration);
    }

    // Notification methods
    private void notifyStart(String name) {
        if (mCallback != null) {
            mMainHandler.post(() -> mCallback.onStart(name));
        }
    }

    public void notifyProgress(String status) {
        if (mCallback != null && mCurrentScript != null) {
            mMainHandler.post(() -> mCallback.onProgress(mCurrentScript, status));
        }
    }

    private void notifyComplete(String name, boolean success, String msg) {
        if (mCallback != null) {
            mMainHandler.post(() -> mCallback.onComplete(name, success, msg));
        }
    }

    private void notifyError(String name, String error) {
        if (mCallback != null) {
            mMainHandler.post(() -> mCallback.onError(name, error));
        }
    }

    public void shutdown() {
        stop();
        mExecutor.shutdown();
    }

    /**
     * Exception thrown when script is stopped.
     */
    public static class ScriptStopException extends RuntimeException {
        public ScriptStopException(String message) {
            super(message);
        }
    }
}
