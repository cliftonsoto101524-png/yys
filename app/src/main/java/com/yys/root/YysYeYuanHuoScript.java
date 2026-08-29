package com.yys.root;

import java.util.Map;

/**
 * Ye Yuan Huo (业原火) automation script.
 */
public class YysYeYuanHuoScript extends YysAuto {

    public YysYeYuanHuoScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Ye Yuan Huo...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("yeyuanhuo");
        int floor = config.containsKey("floor") ? ((Number) config.get("floor")).intValue() : 3;
        int runCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 20;

        if (!navigateToYeYuanHuo(floor)) {
            mEngine.notifyProgress("Failed to navigate to Ye Yuan Huo");
            return;
        }

        enterBattle(runCount);
        mEngine.notifyProgress("Ye Yuan Huo completed");
    }

    private boolean navigateToYeYuanHuo(int floor) {
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("yeyuanhuo", "yeyuanhuo_icon", 10000)) return false;
        mEngine.sleep(3000);

        String floorTemplate = "floor_" + floor;
        if (!mEngine.waitAndClick("yeyuanhuo", floorTemplate, 8000)) return false;
        mEngine.sleep(2000);

        return true;
    }

    @Override
    public String getName() {
        return "业原火";
    }

    @Override
    public String getDescription() {
        return "自动刷业原火副本";
    }

    @Override
    protected String getModule() {
        return "yeyuanhuo";
    }
}
