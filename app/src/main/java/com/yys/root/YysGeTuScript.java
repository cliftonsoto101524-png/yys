package com.yys.root;

import java.util.Map;

/**
 * Ge Tu (觉醒副本 / Skin zone) automation script.
 */
public class YysGeTuScript extends YysAuto {

    public YysGeTuScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Ge Tu (Awakening)...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("getu");
        String element = config.containsKey("element") ? (String) config.get("element") : "fire";
        int floor = config.containsKey("floor") ? ((Number) config.get("floor")).intValue() : 10;
        int runCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 30;

        if (!navigateToGeTu(element, floor)) {
            mEngine.notifyProgress("Failed to navigate to Ge Tu");
            return;
        }

        enterBattle(runCount);
        mEngine.notifyProgress("Ge Tu completed");
    }

    private boolean navigateToGeTu(String element, int floor) {
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("getu", "awakening_icon", 10000)) return false;
        mEngine.sleep(3000);

        // Select element type
        String elementTab = "element_" + element;
        mEngine.clickIfPresent("getu", elementTab);
        mEngine.sleep(1000);

        // Select floor
        String floorTemplate = "floor_" + floor;
        if (!mEngine.waitAndClick("getu", floorTemplate, 8000)) {
            mEngine.swipe(mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] * 3 / 4,
                    mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] / 4);
            mEngine.sleep(1000);
            if (!mEngine.waitAndClick("getu", floorTemplate, 5000)) return false;
        }
        mEngine.sleep(2000);

        return true;
    }

    @Override
    public String getName() {
        return "觉醒副本";
    }

    @Override
    public String getDescription() {
        return "自动刷觉醒副本，支持火/风/水/雷四种属性";
    }

    @Override
    protected String getModule() {
        return "getu";
    }
}
