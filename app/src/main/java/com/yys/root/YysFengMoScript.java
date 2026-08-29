package com.yys.root;

import java.util.Map;

/**
 * Feng Mo Zhi Shi (逢魔之时) automation script.
 */
public class YysFengMoScript extends YysAuto {

    public YysFengMoScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Feng Mo Zhi Shi...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("fengmo");
        boolean doExploration = config.containsKey("exploration") && (Boolean) config.get("exploration");
        boolean doBoss = config.containsKey("boss") && (Boolean) config.get("boss");

        // Navigate to逢魔之时
        if (!navigateToFengMo()) {
            mEngine.notifyProgress("Failed to navigate to Feng Mo");
            return;
        }

        if (doExploration) {
            doFengMoExploration();
        }

        if (doBoss) {
            doFengMoBoss();
        }

        mEngine.notifyProgress("Feng Mo completed");
    }

    private boolean navigateToFengMo() {
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("fengmo", "fengmo_icon", 10000)) return false;
        mEngine.sleep(3000);
        return true;
    }

    private void doFengMoExploration() {
        mEngine.notifyProgress("Exploring Feng Mo map...");
        for (int i = 0; i < 10 && mEngine.isRunning(); i++) {
            // Click on random spots to discover events
            int rx = mEngine.mScreenSize[0] / 4 + (int)(Math.random() * mEngine.mScreenSize[0] / 2);
            int ry = mEngine.mScreenSize[1] / 4 + (int)(Math.random() * mEngine.mScreenSize[1] / 2);
            mEngine.clickAt(rx, ry);
            mEngine.sleep(3000);

            // Handle any discovered events
            if (mEngine.clickIfPresent("fengmo", "event_battle")) {
                mEngine.sleep(2000);
                waitForBattleEnd(300000);
                acceptRewards();
            } else if (mEngine.clickIfPresent("fengmo", "event_reward")) {
                mEngine.sleep(2000);
                mEngine.clickIfPresent("common", "ok");
            }

            mEngine.sleep(2000);
        }
    }

    private void doFengMoBoss() {
        mEngine.notifyProgress("Fighting Feng Mo boss...");
        if (mEngine.waitAndClick("fengmo", "boss_entrance", 10000)) {
            mEngine.sleep(3000);
            if (mEngine.waitAndClick("fengmo", "challenge_boss", 10000)) {
                mEngine.sleep(2000);
                waitForBattleEnd(600000); // Boss may take longer
                acceptRewards();
            }
        }
    }

    @Override
    public String getName() {
        return "逢魔之时";
    }

    @Override
    public String getDescription() {
        return "自动完成逢魔之时探索和BOSS战";
    }

    @Override
    protected String getModule() {
        return "fengmo";
    }
}
