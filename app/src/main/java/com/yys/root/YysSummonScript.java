package com.yys.root;

import java.util.Map;

/**
 * Summon (召唤狗粮) automation script.
 * Automatically summons shikigami for fodder.
 */
public class YysSummonScript extends YysAuto {

    public YysSummonScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting summon...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("summon");
        int summonCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 50;
        boolean useTicket = config.containsKey("use_ticket") && (Boolean) config.get("use_ticket");

        // Navigate to summon
        if (!navigateToSummon()) {
            mEngine.notifyProgress("Failed to navigate to summon");
            return;
        }

        for (int i = 0; i < summonCount && mEngine.isRunning(); i++) {
            mEngine.notifyProgress("Summoning " + (i + 1) + "/" + summonCount);

            // Select summon type
            if (useTicket) {
                mEngine.clickIfPresent("summon", "ticket_summon");
            } else {
                mEngine.clickIfPresent("summon", "jade_summon");
            }
            mEngine.sleep(1000);

            // Confirm summon
            if (mEngine.clickIfPresent("summon", "summon_button")) {
                mEngine.sleep(5000);
                // Skip animation if possible
                mEngine.clickIfPresent("common", "skip");
                mEngine.sleep(3000);
                // Close result
                mEngine.clickIfPresent("common", "close");
                mEngine.sleep(1500);
            } else {
                mEngine.notifyProgress("Summon button not found, may be out of resources");
                break;
            }
        }

        mEngine.notifyProgress("Summon completed");
    }

    private boolean navigateToSummon() {
        if (!mEngine.waitAndClick("common", "summon", 10000)) {
            // Try alternative navigation
            if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
            mEngine.sleep(2000);
            if (!mEngine.waitAndClick("summon", "summon_icon", 10000)) return false;
        }
        mEngine.sleep(3000);
        return true;
    }

    @Override
    public String getName() {
        return "召唤狗粮";
    }

    @Override
    public String getDescription() {
        return "自动召唤式神获取狗粮";
    }

    @Override
    protected String getModule() {
        return "summon";
    }
}
