package com.yys.root;

import java.util.Map;

/**
 * Jie Jie Ka Ji Yang (结界卡寄养) automation script.
 * Places shikigami in friends' realm cards.
 */
public class YysJiYangScript extends YysAuto {

    public YysJiYangScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Jie Jie Ka Ji Yang...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("jiyang");
        boolean useTaiGu = config.containsKey("use_taigu") && (Boolean) config.get("use_taigu");

        // Navigate to结界
        if (!navigateToJieJie()) {
            mEngine.notifyProgress("Failed to navigate to Jie Jie");
            return;
        }

        // Go to friends' list for寄养
        if (!mEngine.waitAndClick("jiyang", "friend_jie_jie", 8000)) return;
        mEngine.sleep(3000);

        // Find and occupy available slots
        occupySlots(useTaiGu);

        mEngine.notifyProgress("Ji Yang completed");
    }

    private boolean navigateToJieJie() {
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("jiyang", "jie_jie_icon", 10000)) return false;
        mEngine.sleep(3000);
        return true;
    }

    private void occupySlots(boolean useTaiGu) {
        mEngine.notifyProgress("Looking for available slots...");

        for (int friend = 0; friend < 20 && mEngine.isRunning(); friend++) {
            // Check if this friend has an empty slot
            ImageMatcher.MatchResult slot = mEngine.findTemplate("jiyang", "empty_slot");
            if (slot.found) {
                mEngine.clickAt(slot.centerX(), slot.centerY());
                mEngine.sleep(2000);

                // Select card type
                if (useTaiGu) {
                    mEngine.clickIfPresent("jiyang", "taigu_tab");
                } else {
                    mEngine.clickIfPresent("jiyang", "douyu_tab");
                }
                mEngine.sleep(1000);

                // Confirm
                mEngine.clickIfPresent("jiyang", "confirm_place");
                mEngine.sleep(2000);

                // Check if successful
                if (mEngine.clickIfPresent("common", "ok")) {
                    mEngine.sleep(1000);
                }

                // Try next friend
                mEngine.swipe(mEngine.mScreenSize[0] * 3 / 4, mEngine.mScreenSize[1] / 2,
                        mEngine.mScreenSize[0] / 4, mEngine.mScreenSize[1] / 2);
                mEngine.sleep(1500);
            } else {
                // Scroll to find more friends
                mEngine.swipe(mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] * 3 / 4,
                        mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] / 4);
                mEngine.sleep(1500);
            }
        }
    }

    @Override
    public String getName() {
        return "结界卡寄养";
    }

    @Override
    public String getDescription() {
        return "自动在好友结界中寄养式神";
    }

    @Override
    protected String getModule() {
        return "jiyang";
    }
}
