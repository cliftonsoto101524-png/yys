package com.yys.root;

import java.util.Map;

/**
 * Hunting battle (狩猎战) automation script.
 */
public class YysHuntingScript extends YysAuto {

    public YysHuntingScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting hunting battle...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("hunting");
        int runCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 10;

        // Navigate to guild hunting
        if (!navigateToHunting()) {
            mEngine.notifyProgress("Failed to navigate to hunting");
            return;
        }

        for (int i = 0; i < runCount && mEngine.isRunning(); i++) {
            mEngine.notifyProgress("Hunting run " + (i + 1) + "/" + runCount);

            // Enter hunting
            if (!mEngine.waitAndClick("hunting", "enter_hunting", 10000)) {
                // Maybe hunting not active or maxed out
                mEngine.notifyProgress("Hunting may not be available");
                break;
            }
            mEngine.sleep(3000);

            // Wait for battle end
            boolean victory = waitForBattleEnd(300000);
            acceptRewards();
            handlePopups();

            if (!victory) {
                mEngine.notifyProgress("Hunting battle failed");
            }

            mEngine.sleep(2000);
        }

        mEngine.notifyProgress("Hunting completed");
    }

    private boolean navigateToHunting() {
        // Go to guild
        if (!mEngine.waitAndClick("common", "guild", 10000)) return false;
        mEngine.sleep(3000);
        // Click hunting
        if (!mEngine.waitAndClick("hunting", "hunting_entrance", 10000)) return false;
        mEngine.sleep(3000);
        return true;
    }

    @Override
    public String getName() {
        return "狩猎战";
    }

    @Override
    public String getDescription() {
        return "自动参与寮狩猎战";
    }

    @Override
    protected String getModule() {
        return "hunting";
    }
}
