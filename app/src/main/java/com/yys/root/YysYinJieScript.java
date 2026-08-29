package com.yys.root;

import java.util.Map;

/**
 * Yin Jie Zhi Men (阴界之门) automation script.
 */
public class YysYinJieScript extends YysAuto {

    public YysYinJieScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Yin Jie Zhi Men...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("yinjie");
        int targetFloor = config.containsKey("target_floor") ? ((Number) config.get("target_floor")).intValue() : 50;

        if (!navigateToYinJie()) {
            mEngine.notifyProgress("Failed to navigate to Yin Jie");
            return;
        }

        // Start and climb floors
        int currentFloor = 1;
        while (currentFloor <= targetFloor && mEngine.isRunning()) {
            mEngine.notifyProgress("Yin Jie floor " + currentFloor + "/" + targetFloor);

            if (!mEngine.waitAndClick("yinjie", "start_battle", 15000)) {
                mEngine.notifyProgress("Battle start failed, may be completed");
                break;
            }
            mEngine.sleep(2000);

            boolean victory = waitForBattleEnd(300000);
            acceptRewards();
            handlePopups();

            if (!victory) {
                mEngine.notifyProgress("Defeated at floor " + currentFloor);
                break;
            }

            currentFloor++;
            mEngine.sleep(2000);
        }

        mEngine.notifyProgress("Yin Jie completed at floor " + (currentFloor - 1));
    }

    private boolean navigateToYinJie() {
        // Go to guild
        if (!mEngine.waitAndClick("common", "guild", 10000)) return false;
        mEngine.sleep(3000);
        // Click阴界之门
        if (!mEngine.waitAndClick("yinjie", "yinjie_entrance", 10000)) return false;
        mEngine.sleep(3000);
        return true;
    }

    @Override
    public String getName() {
        return "阴界之门";
    }

    @Override
    public String getDescription() {
        return "自动挑战阴界之门，可指定目标层数";
    }

    @Override
    protected String getModule() {
        return "yinjie";
    }
}
