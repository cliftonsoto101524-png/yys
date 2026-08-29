package com.yys.root;

import java.util.Map;

/**
 * Soul zone (Hun Tu /御魂) automation script.
 * Automates soul farming runs.
 */
public class YysHunTuScript extends YysAuto {

    public YysHunTuScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting soul zone...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("huntu");
        int floor = config.containsKey("floor") ? ((Number) config.get("floor")).intValue() : 11;
        boolean isOrochi = config.containsKey("orochi") && (Boolean) config.get("orochi");
        int runCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 50;

        // Navigate to soul zone
        if (!navigateToSoulZone(floor, isOrochi)) {
            mEngine.notifyProgress("Failed to navigate to soul zone");
            return;
        }

        // Start runs
        enterBattle(runCount);

        mEngine.notifyProgress("Soul zone completed");
    }

    private boolean navigateToSoulZone(int floor, boolean isOrochi) {
        mEngine.notifyProgress("Navigating to soul zone floor " + floor);

        // Click explore
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);

        // Click soul zone
        if (!mEngine.waitAndClick("huntu", "soul_zone_icon", 10000)) return false;
        mEngine.sleep(3000);

        // Select floor
        String floorTemplate = "floor_" + floor;
        if (!mEngine.waitAndClick("huntu", floorTemplate, 8000)) {
            // Try scrolling to find floor
            mEngine.swipe(mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] * 3 / 4,
                    mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] / 4);
            mEngine.sleep(1000);
            if (!mEngine.waitAndClick("huntu", floorTemplate, 5000)) return false;
        }
        mEngine.sleep(2000);

        // Select orochi if needed
        if (isOrochi) {
            mEngine.clickIfPresent("huntu", "orochi_tab");
            mEngine.sleep(1000);
        }

        return true;
    }

    @Override
    public String getName() {
        return "御魂副本";
    }

    @Override
    public String getDescription() {
        return "自动刷御魂副本，支持指定层数和八岐大蛇";
    }

    @Override
    protected String getModule() {
        return "huntu";
    }
}
