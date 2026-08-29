package com.yys.root;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced Root command executor with stability and compatibility improvements.
 * Manages a persistent root shell session to avoid repeated su overhead.
 */
public class RootShell {

    private static final String TAG = "RootShell";
    private static final int COMMAND_TIMEOUT_MS = 30000;

    private static RootShell sInstance;
    private Process mProcess;
    private DataOutputStream mStdIn;
    private BufferedReader mStdOut;
    private BufferedReader mStdErr;
    private boolean mIsRootAvailable = false;

    private RootShell() {}

    public static synchronized RootShell getInstance() {
        if (sInstance == null) {
            sInstance = new RootShell();
        }
        return sInstance;
    }

    /**
     * Initialize persistent root shell.
     * @return true if root access was granted
     */
    public synchronized boolean init() {
        if (mProcess != null && mProcess.isAlive()) {
            return mIsRootAvailable;
        }
        try {
            mProcess = Runtime.getRuntime().exec("su");
            mStdIn = new DataOutputStream(mProcess.getOutputStream());
            mStdOut = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            mStdErr = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));

            // Test root access
            mStdIn.writeBytes("id\n");
            mStdIn.flush();

            String line;
            long start = System.currentTimeMillis();
            while ((line = mStdOut.readLine()) != null) {
                if (line.contains("uid=0")) {
                    mIsRootAvailable = true;
                    break;
                }
                if (System.currentTimeMillis() - start > 5000) {
                    break;
                }
            }

            if (!mIsRootAvailable) {
                close();
            }
            return mIsRootAvailable;
        } catch (IOException e) {
            Log.e(TAG, "Failed to init root shell", e);
            mIsRootAvailable = false;
            return false;
        }
    }

    /**
     * Execute a single command and return output.
     */
    public synchronized String execute(String command) {
        if (!mIsRootAvailable && !init()) {
            Log.w(TAG, "Root not available, cannot execute: " + command);
            return null;
        }
        try {
            mStdIn.writeBytes(command + "\n");
            mStdIn.writeBytes("echo ENDOFCOMMAND\n");
            mStdIn.flush();

            StringBuilder output = new StringBuilder();
            String line;
            long start = System.currentTimeMillis();
            while ((line = mStdOut.readLine()) != null) {
                if ("ENDOFCOMMAND".equals(line)) {
                    break;
                }
                if (output.length() > 0) {
                    output.append("\n");
                }
                output.append(line);
                if (System.currentTimeMillis() - start > COMMAND_TIMEOUT_MS) {
                    Log.w(TAG, "Command timeout: " + command);
                    break;
                }
            }
            return output.toString();
        } catch (IOException e) {
            Log.e(TAG, "Command execution failed: " + command, e);
            // Try to reinitialize
            close();
            init();
            return null;
        }
    }

    /**
     * Execute command without waiting for output.
     */
    public synchronized void executeAsync(String command) {
        if (!mIsRootAvailable && !init()) {
            return;
        }
        try {
            mStdIn.writeBytes(command + "\n");
            mStdIn.flush();
        } catch (IOException e) {
            Log.e(TAG, "Async command failed", e);
        }
    }

    /**
     * Simulate touch tap at screen coordinates.
     */
    public void tap(int x, int y) {
        execute("input tap " + x + " " + y);
        int delay = ConfigManager.getInstance().getClickDelay();
        sleep(delay);
    }

    /**
     * Simulate swipe gesture.
     */
    public void swipe(int x1, int y1, int x2, int y2) {
        int duration = ConfigManager.getInstance().getSwipeDuration();
        execute("input swipe " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + duration);
        sleep(duration + 200);
    }

    /**
     * Simulate long press.
     */
    public void longPress(int x, int y, int durationMs) {
        execute("input swipe " + x + " " + y + " " + x + " " + y + " " + durationMs);
        sleep(durationMs + 200);
    }

    /**
     * Simulate text input.
     */
    public void inputText(String text) {
        execute("input text '" + text.replace("'", "'\"'\"'") + "'");
        sleep(300);
    }

    /**
     * Simulate key event.
     */
    public void sendKey(int keyCode) {
        execute("input keyevent " + keyCode);
        sleep(200);
    }

    /**
     * Take screenshot via screencap and save to path.
     */
    public boolean screenshot(String path) {
        String result = execute("screencap -p " + path);
        return result != null;
    }

    /**
     * Get screen resolution.
     * @return int[]{width, height}
     */
    public int[] getScreenSize() {
        String result = execute("wm size");
        if (result != null && result.contains("Physical size:")) {
            String size = result.substring(result.indexOf("Physical size:") + 14).trim();
            String[] parts = size.split("x");
            if (parts.length == 2) {
                try {
                    return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
                } catch (NumberFormatException ignored) {}
            }
        }
        return new int[]{1080, 1920}; // Default fallback
    }

    /**
     * Check if a package is running.
     */
    public boolean isPackageRunning(String packageName) {
        String result = execute("dumpsys activity activities | grep " + packageName);
        return result != null && !result.isEmpty();
    }

    /**
     * Force stop an application.
     */
    public void forceStop(String packageName) {
        execute("am force-stop " + packageName);
        sleep(1000);
    }

    /**
     * Start an activity.
     */
    public void startActivity(String packageName, String activityName) {
        execute("am start -n " + packageName + "/" + activityName);
        sleep(3000);
    }

    /**
     * Check if root is available.
     */
    public boolean isRootAvailable() {
        return mIsRootAvailable;
    }

    /**
     * Close the persistent shell.
     */
    public synchronized void close() {
        try {
            if (mStdIn != null) {
                mStdIn.writeBytes("exit\n");
                mStdIn.close();
            }
            if (mStdOut != null) mStdOut.close();
            if (mStdErr != null) mStdErr.close();
            if (mProcess != null) mProcess.destroy();
        } catch (IOException e) {
            Log.e(TAG, "Error closing shell", e);
        } finally {
            mProcess = null;
            mStdIn = null;
            mStdOut = null;
            mStdErr = null;
            mIsRootAvailable = false;
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
