package com.yys.root;

import java.util.Map;

/**
 * Bai Gui Ye Xing (百鬼夜行) automation script.
 * Automates the ghost parade mini-game.
 */
public class YysBaiGuiScript extends YysAuto {

    public YysBaiGuiScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Bai Gui Ye Xing...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("baigui");
        int ticketCount = config.containsKey("tickets") ? ((Number) config.get("tickets")).intValue() : 10;

        // Navigate to Bai Gui
        if (!navigateToBaiGui()) {
            mEngine.notifyProgress("Failed to navigate to Bai Gui");
            return;
        }

        for (int i = 0; i < ticketCount && mEngine.isRunning(); i++) {
            mEngine.notifyProgress("Bai Gui run " + (i + 1) + "/" + ticketCount);
            playBaiGuiRound();
        }

        mEngine.notifyProgress("Bai Gui completed");
    }

    private boolean navigateToBaiGui() {
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("baigui", "baigui_icon", 10000)) return false;
        mEngine.sleep(3000);
        return true;
    }

    private void playBaiGuiRound() {
        // Start round
        if (!mEngine.waitAndClick("baigui", "start_baigui", 8000)) return;
        mEngine.sleep(5000);

        // During the game, continuously throw beans
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 35000 && mEngine.isRunning()) {
            mEngine.checkPause();
            // Look for high-value targets (SSR/SP)
            ImageMatcher.MatchResult ssr = mEngine.findTemplate("baigui", "ssr_target");
            if (ssr.found) {
                // Throw more beans at SSR
                mEngine.clickAt(ssr.centerX(), ssr.centerY());
                mEngine.sleep(200);
                mEngine.clickAt(ssr.centerX(), ssr.centerY());
            } else {
                // Throw at any visible target
                ImageMatcher.MatchResult target = mEngine.findTemplate("baigui", "target");
                if (target.found) {
                    mEngine.clickAt(target.centerX(), target.centerY());
                }
            }
            mEngine.sleep(300);
        }

        // Wait for result and continue
        mEngine.sleep(3000);
        mEngine.clickIfPresent("common", "ok");
        mEngine.sleep(2000);
    }

    @Override
    public String getName() {
        return "百鬼夜行";
    }

    @Override
    public String getDescription() {
        return "自动百鬼夜行，优先砸SSR/SP式神";
    }

    @Override
    protected String getModule() {
        return "baigui";
    }
}
