package com.yys.root;

/**
 * Xiao Zhi Ren (庭院小纸人) automation script.
 * Collects daily rewards from the paper doll in courtyard.
 */
public class YysXiaoZhiRenScript extends YysAuto {

    public YysXiaoZhiRenScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Xiao Zhi Ren collection...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        // Ensure we're on main screen
        mEngine.sleep(3000);

        // Look for paper doll
        for (int i = 0; i < 5; i++) {
            ImageMatcher.MatchResult doll = mEngine.findTemplate("xiaozhiren", "paper_doll");
            if (doll.found) {
                mEngine.clickAt(doll.centerX(), doll.centerY());
                mEngine.sleep(3000);

                // Collect all rewards
                collectRewards();

                // Close reward dialog
                mEngine.clickIfPresent("common", "close");
                mEngine.sleep(1000);

                break;
            } else {
                // Maybe on different screen, try to go home
                mEngine.clickIfPresent("common", "home");
                mEngine.sleep(3000);
            }
        }

        mEngine.notifyProgress("Xiao Zhi Ren completed");
    }

    private void collectRewards() {
        // Click all available reward items
        for (int i = 0; i < 10; i++) {
            if (mEngine.clickIfPresent("xiaozhiren", "reward_item")) {
                mEngine.sleep(1500);
                mEngine.clickIfPresent("common", "ok");
                mEngine.sleep(1000);
            } else {
                break;
            }
        }
    }

    @Override
    public String getName() {
        return "庭院小纸人";
    }

    @Override
    public String getDescription() {
        return "自动点击庭院小纸人领取奖励";
    }

    @Override
    protected String getModule() {
        return "xiaozhiren";
    }
}
