package com.yys.root;

/**
 * Reconnect (卡死重连) automation script.
 * Monitors game state and handles reconnect/stuck situations.
 */
public class YysReconnectScript extends YysAuto implements Runnable {

    private final Thread mMonitorThread;
    private boolean mMonitoring = false;

    public YysReconnectScript(ScriptEngine engine) {
        super(engine);
        mMonitorThread = new Thread(this);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting reconnect monitor...");
        mMonitoring = true;

        int stuckCounter = 0;
        long lastScreenChange = System.currentTimeMillis();
        String lastScreenHash = "";

        while (mMonitoring && mEngine.isRunning()) {
            mEngine.checkPause();

            // Check if game is running
            if (!mEngine.isGameRunning()) {
                mEngine.notifyProgress("Game not running, attempting restart...");
                mEngine.startGame();
                mEngine.sleep(20000);
                stuckCounter = 0;
                continue;
            }

            // Take screenshot and check for stuck indicators
            ImageMatcher.MatchResult reconnectBtn = mEngine.findTemplate("common", "reconnect_button");
            if (reconnectBtn.found) {
                mEngine.notifyProgress("Reconnect button detected, clicking...");
                mEngine.clickAt(reconnectBtn.centerX(), reconnectBtn.centerY());
                mEngine.sleep(ConfigManager.getInstance().getReconnectTimeout());
                stuckCounter = 0;
                continue;
            }

            // Check for network error
            if (mEngine.clickIfPresent("common", "network_retry")) {
                mEngine.notifyProgress("Network retry detected");
                mEngine.sleep(10000);
                stuckCounter = 0;
                continue;
            }

            // Check for maintenance/disconnect
            if (mEngine.clickIfPresent("common", "disconnect_ok")) {
                mEngine.notifyProgress("Disconnect dialog detected");
                mEngine.sleep(5000);
                stuckCounter = 0;
                continue;
            }

            // Detect stuck by checking if screen hasn't changed
            if (ConfigManager.getInstance().isAutoReconnect()) {
                String currentHash = getScreenHash();
                if (currentHash.equals(lastScreenHash)) {
                    stuckCounter++;
                    if (stuckCounter > 30) { // ~60 seconds stuck
                        mEngine.notifyProgress("Screen stuck detected, forcing reconnect...");
                        handleStuckReconnect();
                        stuckCounter = 0;
                    }
                } else {
                    stuckCounter = 0;
                    lastScreenHash = currentHash;
                    lastScreenChange = System.currentTimeMillis();
                }
            }

            // Sleep between checks
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        mMonitoring = false;
        mEngine.notifyProgress("Reconnect monitor stopped");
    }

    private String getScreenHash() {
        // Simple hash based on pixel sampling
        android.graphics.Bitmap screenshot = mEngine.takeScreenshot();
        if (screenshot == null) return "";

        try {
            int w = screenshot.getWidth();
            int h = screenshot.getHeight();
            long hash = 0;
            // Sample 9 points
            int[] xs = {w / 4, w / 2, w * 3 / 4};
            int[] ys = {h / 4, h / 2, h * 3 / 4};
            for (int x : xs) {
                for (int y : ys) {
                    hash = hash * 31 + screenshot.getPixel(x, y);
                }
            }
            return String.valueOf(hash);
        } finally {
            screenshot.recycle();
        }
    }

    private void handleStuckReconnect() {
        // Try to click back button
        mEngine.clickIfPresent("common", "back");
        mEngine.sleep(3000);

        // If still stuck, restart game
        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(20000);
            return;
        }

        // Check for reconnect button again
        ImageMatcher.MatchResult reconnect = mEngine.findTemplate("common", "reconnect_button");
        if (reconnect.found) {
            mEngine.clickAt(reconnect.centerX(), reconnect.centerY());
            mEngine.sleep(ConfigManager.getInstance().getReconnectTimeout());
        }
    }

    public void startMonitoring() {
        if (!mMonitoring) {
            mMonitoring = true;
            mMonitorThread.start();
        }
    }

    public void stopMonitoring() {
        mMonitoring = false;
        mMonitorThread.interrupt();
    }

    public boolean isMonitoring() {
        return mMonitoring;
    }

    @Override
    public String getName() {
        return "卡死重连";
    }

    @Override
    public String getDescription() {
        return "监控游戏状态，自动处理断线重连和卡死";
    }

    @Override
    protected String getModule() {
        return "reconnect";
    }
}
