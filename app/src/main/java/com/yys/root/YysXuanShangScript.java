package com.yys.root;

/**
 * Xuan Shang Feng Yin (悬赏封印) automation script.
 */
public class YysXuanShangScript extends YysAuto {

    public YysXuanShangScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Xuan Shang Feng Yin...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        // Navigate to悬赏封印
        if (!navigateToXuanShang()) {
            mEngine.notifyProgress("Failed to navigate to Xuan Shang");
            return;
        }

        // Accept all available悬赏
        acceptAllXuanShang();

        // Try to complete them
        completeXuanShang();

        mEngine.notifyProgress("Xuan Shang completed");
    }

    private boolean navigateToXuanShang() {
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("xuanshang", "xuanshang_icon", 10000)) return false;
        mEngine.sleep(3000);
        return true;
    }

    private void acceptAllXuanShang() {
        mEngine.notifyProgress("Accepting悬赏...");
        for (int i = 0; i < 5; i++) {
            if (mEngine.clickIfPresent("xuanshang", "accept_xuanshang")) {
                mEngine.sleep(1000);
                mEngine.clickIfPresent("common", "confirm");
                mEngine.sleep(1000);
            } else {
                break;
            }
        }
    }

    private void completeXuanShang() {
        mEngine.notifyProgress("Completing悬赏...");
        // Check each悬赏 and navigate to corresponding dungeon
        for (int i = 0; i < 5; i++) {
            if (mEngine.clickIfPresent("xuanshang", "goto_target")) {
                mEngine.sleep(5000);
                // Battle once
                if (mEngine.waitAndClick("common", "start_battle", 10000)) {
                    waitForBattleEnd(300000);
                    acceptRewards();
                }
                // Go back to悬赏 page
                mEngine.clickIfPresent("common", "back");
                mEngine.sleep(2000);
            } else {
                break;
            }
        }
    }

    @Override
    public String getName() {
        return "悬赏封印";
    }

    @Override
    public String getDescription() {
        return "自动接受和完成悬赏封印任务";
    }

    @Override
    protected String getModule() {
        return "xuanshang";
    }
}
