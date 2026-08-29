package com.yys.root;

import java.util.Map;

/**
 * Daily task automation script.
 * Handles daily sign-in, missions, and routine activities.
 */
public class YysDailyScript extends YysAuto {

    public YysDailyScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting daily tasks...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000); // Wait for game to load
        }

        // Daily sign-in
        doSignIn();

        // Daily missions
        doDailyMissions();

        // Free summons
        doFreeSummon();

        // Shop check
        doShopCheck();

        // Friend kisses
        doFriendKisses();

        // Guild contribution
        doGuildContribution();

        mEngine.notifyProgress("Daily tasks completed");
    }

    private void doSignIn() {
        mEngine.notifyProgress("Doing daily sign-in...");
        mEngine.clickIfPresent("common", "sign_in");
        mEngine.sleep(2000);
        mEngine.clickIfPresent("common", "sign_in_confirm");
        mEngine.sleep(1000);
        mEngine.clickIfPresent("common", "close");
    }

    private void doDailyMissions() {
        mEngine.notifyProgress("Checking daily missions...");
        if (mEngine.waitAndClick("daily", "mission_button", 5000)) {
            mEngine.sleep(2000);
            // Claim all available rewards
            for (int i = 0; i < 10; i++) {
                if (!mEngine.clickIfPresent("daily", "claim_reward")) break;
                mEngine.sleep(1000);
                mEngine.clickIfPresent("common", "ok");
                mEngine.sleep(500);
            }
            mEngine.clickIfPresent("common", "back");
        }
    }

    private void doFreeSummon() {
        mEngine.notifyProgress("Checking free summon...");
        navigateTo("summon", "free_summon_check");
        if (mEngine.clickIfPresent("daily", "free_summon")) {
            mEngine.sleep(3000);
            mEngine.clickIfPresent("common", "skip");
            mEngine.sleep(2000);
        }
        mEngine.clickIfPresent("common", "back");
    }

    private void doShopCheck() {
        mEngine.notifyProgress("Checking shop...");
        navigateTo("shop", "shop_enter");
        // Check for free items
        mEngine.clickIfPresent("daily", "free_item");
        mEngine.sleep(1000);
        mEngine.clickIfPresent("common", "confirm");
        mEngine.sleep(1000);
        mEngine.clickIfPresent("common", "back");
    }

    private void doFriendKisses() {
        mEngine.notifyProgress("Doing friend kisses...");
        navigateTo("friend", "friend_list");
        for (int i = 0; i < 20; i++) {
            if (mEngine.clickIfPresent("daily", "kiss_button")) {
                mEngine.sleep(500);
            } else {
                break;
            }
        }
        mEngine.clickIfPresent("common", "back");
    }

    private void doGuildContribution() {
        mEngine.notifyProgress("Doing guild contribution...");
        navigateTo("guild", "guild_enter");
        mEngine.clickIfPresent("daily", "contribute_button");
        mEngine.sleep(2000);
        mEngine.clickIfPresent("daily", "contribute_confirm");
        mEngine.sleep(1000);
        mEngine.clickIfPresent("common", "back");
    }

    @Override
    public String getName() {
        return "日常任务";
    }

    @Override
    public String getDescription() {
        return "自动完成每日签到、任务、免费召唤、商店、好友和寮贡献";
    }

    @Override
    protected String getModule() {
        return "daily";
    }
}
