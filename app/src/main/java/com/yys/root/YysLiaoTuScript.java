package com.yys.root;

import java.util.Map;

/**
 * Liao Tu (御灵副本 / Beast zone) automation script.
 */
public class YysLiaoTuScript extends YysAuto {

    public YysLiaoTuScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Liao Tu (Beast zone)...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("liaotu");
        String beast = config.containsKey("beast") ? (String) config.get("beast") : "dragon";
        int floor = config.containsKey("floor") ? ((Number) config.get("floor")).intValue() : 3;
        int runCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 20;

        if (!navigateToLiaoTu(beast, floor)) {
            mEngine.notifyProgress("Failed to navigate to Liao Tu");
            return;
        }

        enterBattle(runCount);
        mEngine.notifyProgress("Liao Tu completed");
    }

    private boolean navigateToLiaoTu(String beast, int floor) {
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("liaotu", "beast_zone_icon", 10000)) return false;
        mEngine.sleep(3000);

        // Select beast
        String beastTab = "beast_" + beast;
        mEngine.clickIfPresent("liaotu", beastTab);
        mEngine.sleep(1000);

        // Select floor
        String floorTemplate = "floor_" + floor;
        if (!mEngine.waitAndClick("liaotu", floorTemplate, 8000)) return false;
        mEngine.sleep(2000);

        return true;
    }

    @Override
    public String getName() {
        return "御灵副本";
    }

    @Override
    public String getDescription() {
        return "自动刷御灵副本，支持神龙/白藏主/黑豹/孔雀";
    }

    @Override
    protected String getModule() {
        return "liaotu";
    }
}
