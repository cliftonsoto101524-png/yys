package com.yys.root;

import java.util.Map;

/**
 * UP Select (UP选择) automation script.
 * Handles UP rate shikigami selection.
 */
public class YysUpSelectScript extends YysAuto {

    public YysUpSelectScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting UP Select...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("upselect");
        String[] targetShikigami = config.containsKey("targets")
                ? ((String) config.get("targets")).split(",")
                : new String[]{"sp"};

        // Navigate to summon UP screen
        if (!navigateToUpSelect()) {
            mEngine.notifyProgress("Failed to navigate to UP Select");
            return;
        }

        // Select each target
        for (String target : targetShikigami) {
            mEngine.notifyProgress("Selecting UP: " + target);
            String templateName = "up_" + target.trim().toLowerCase();
            if (mEngine.clickIfPresent("upselect", templateName)) {
                mEngine.sleep(1000);
            }
        }

        // Confirm selection
        mEngine.clickIfPresent("upselect", "confirm_up");
        mEngine.sleep(2000);
        mEngine.clickIfPresent("common", "ok");

        mEngine.notifyProgress("UP Select completed");
    }

    private boolean navigateToUpSelect() {
        if (!navigateToSummon()) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("upselect", "up_select_tab", 8000)) return false;
        mEngine.sleep(2000);
        return true;
    }

    private boolean navigateToSummon() {
        if (!mEngine.waitAndClick("common", "summon", 10000)) {
            if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
            mEngine.sleep(2000);
            if (!mEngine.waitAndClick("summon", "summon_icon", 10000)) return false;
        }
        mEngine.sleep(3000);
        return true;
    }

    @Override
    public String getName() {
        return "UP选择";
    }

    @Override
    public String getDescription() {
        return "自动选择UP概率式神";
    }

    @Override
    protected String getModule() {
        return "upselect";
    }
}
