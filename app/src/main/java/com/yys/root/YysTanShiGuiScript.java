package com.yys.root;

import java.util.Map;

/**
 * Tan Shi Gui (贪食鬼吃御魂) automation script.
 * Automatically feeds souls to the hungry ghost.
 */
public class YysTanShiGuiScript extends YysAuto {

    public YysTanShiGuiScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Tan Shi Gui...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("tanshigui");
        int feedCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 100;

        // Navigate to shikigami
        if (!navigateToTanShiGui()) {
            mEngine.notifyProgress("Failed to navigate to Tan Shi Gui");
            return;
        }

        // Feed souls
        for (int i = 0; i < feedCount && mEngine.isRunning(); i++) {
            mEngine.notifyProgress("Feeding " + (i + 1) + "/" + feedCount);

            if (!mEngine.clickIfPresent("tanshigui", "feed_button")) {
                mEngine.notifyProgress("Feed button not found, may be full");
                break;
            }
            mEngine.sleep(2000);

            // Confirm feed
            mEngine.clickIfPresent("common", "confirm");
            mEngine.sleep(2000);

            // Handle level up popups
            mEngine.clickIfPresent("common", "ok");
            mEngine.sleep(1000);
        }

        mEngine.notifyProgress("Tan Shi Gui completed");
    }

    private boolean navigateToTanShiGui() {
        if (!mEngine.waitAndClick("common", "shikigami", 10000)) return false;
        mEngine.sleep(3000);
        // Find and select Tan Shi Gui
        if (!mEngine.waitAndClick("tanshigui", "tanshigui_tab", 8000)) {
            // Scroll to find it
            mEngine.swipe(mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] * 3 / 4,
                    mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] / 4);
            mEngine.sleep(1000);
            if (!mEngine.waitAndClick("tanshigui", "tanshigui_tab", 5000)) return false;
        }
        mEngine.sleep(2000);
        return true;
    }

    @Override
    public String getName() {
        return "贪食鬼吃御魂";
    }

    @Override
    public String getDescription() {
        return "自动用贪食鬼吃御魂升级";
    }

    @Override
    protected String getModule() {
        return "tanshigui";
    }
}
