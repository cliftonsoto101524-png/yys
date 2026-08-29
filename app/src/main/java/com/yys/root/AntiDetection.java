package com.yys.root;

import android.util.Log;

import java.util.Random;

/**
 * Anti-detection module to reduce automation pattern recognition.
 * Adds randomization to timing, touch patterns, and behavior.
 */
public class AntiDetection {

    private static final String TAG = "AntiDetection";
    private static final Random sRandom = new Random();

    private boolean mEnabled;
    private long mBaseClickDelay;
    private long mBaseSwipeDuration;

    public AntiDetection() {
        mEnabled = ConfigManager.getInstance().isAntiDetectionEnabled();
        mBaseClickDelay = ConfigManager.getInstance().getClickDelay();
        mBaseSwipeDuration = ConfigManager.getInstance().getSwipeDuration();
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Get randomized click delay with human-like variance.
     */
    public long getRandomizedClickDelay() {
        if (!mEnabled) return mBaseClickDelay;
        // Add 10-40% variance
        double factor = 1.0 + (sRandom.nextDouble() * 0.3 + 0.1);
        return (long) (mBaseClickDelay * factor);
    }

    /**
     * Get randomized swipe duration.
     */
    public long getRandomizedSwipeDuration() {
        if (!mEnabled) return mBaseSwipeDuration;
        double factor = 1.0 + (sRandom.nextDouble() * 0.2 - 0.1);
        return (long) (mBaseSwipeDuration * factor);
    }

    /**
     * Add random offset to coordinates to avoid identical touch points.
     */
    public int[] randomizeCoordinates(int x, int y, int maxOffset) {
        if (!mEnabled) return new int[]{x, y};
        int offsetX = sRandom.nextInt(maxOffset * 2 + 1) - maxOffset;
        int offsetY = sRandom.nextInt(maxOffset * 2 + 1) - maxOffset;
        return new int[]{x + offsetX, y + offsetY};
    }

    /**
     * Generate random human-like swipe path with slight curvature.
     */
    public int[][] generateSwipePath(int x1, int y1, int x2, int y2, int steps) {
        int[][] path = new int[steps][2];
        for (int i = 0; i < steps; i++) {
            double t = (double) i / (steps - 1);
            int baseX = (int) (x1 + (x2 - x1) * t);
            int baseY = (int) (y1 + (y2 - y1) * t);

            if (mEnabled && i > 0 && i < steps - 1) {
                // Add slight curve variation
                int curve = (int) (Math.sin(t * Math.PI) * sRandom.nextInt(10));
                baseX += curve;
            }
            path[i][0] = baseX;
            path[i][1] = baseY;
        }
        return path;
    }

    /**
     * Random sleep with human-like pattern.
     */
    public void randomSleep(long baseMs) {
        long delay = baseMs;
        if (mEnabled) {
            delay = (long) (baseMs * (1.0 + sRandom.nextDouble() * 0.3));
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Random sleep between min and max milliseconds.
     */
    public void sleepBetween(long minMs, long maxMs) {
        long delay = minMs + (long) (sRandom.nextDouble() * (maxMs - minMs));
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Occasionally perform a random benign action.
     */
    public void maybeDoRandomAction(RootShell shell, int screenW, int screenH) {
        if (!mEnabled) return;
        if (sRandom.nextInt(100) < 5) { // 5% chance
            int action = sRandom.nextInt(3);
            switch (action) {
                case 0:
                    // Slight screen touch
                    int rx = sRandom.nextInt(screenW);
                    int ry = sRandom.nextInt(screenH);
                    shell.tap(rx, ry);
                    break;
                case 1:
                    // Brief pause
                    randomSleep(500);
                    break;
                case 2:
                    // Small swipe
                    int sx = sRandom.nextInt(screenW);
                    int sy = sRandom.nextInt(screenH);
                    shell.swipe(sx, sy, sx + sRandom.nextInt(50), sy + sRandom.nextInt(50));
                    break;
            }
        }
    }

    /**
     * Randomize the match threshold slightly.
     */
    public float getRandomizedThreshold(float baseThreshold) {
        if (!mEnabled) return baseThreshold;
        float variance = (float) (sRandom.nextDouble() * 0.02 - 0.01);
        return Math.max(0.7f, Math.min(0.99f, baseThreshold + variance));
    }
}
