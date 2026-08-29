package com.yys.root;

import java.util.Map;

import android.util.Log;

/**
 * Feng Mo Answer (逢魔答题) automation script.
 * Automatically answers Feng Mo quiz questions.
 */
public class YysFengMoAnswerScript extends YysAuto {

    private static final String TAG = "YysFengMoAnswer";

    public YysFengMoAnswerScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting Feng Mo Answer...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("fengmo_answer");
        int answerCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 10;

        // Navigate to逢魔答题
        if (!navigateToFengMoAnswer()) {
            mEngine.notifyProgress("Failed to navigate to Feng Mo Answer");
            return;
        }

        for (int i = 0; i < answerCount && mEngine.isRunning(); i++) {
            mEngine.notifyProgress("Answering question " + (i + 1) + "/" + answerCount);

            // Wait for question to appear
            mEngine.sleep(5000);

            // Capture question text area (for OCR if available)
            // For now, just select first option
            if (mEngine.clickIfPresent("fengmo_answer", "option_a")) {
                mEngine.sleep(3000);
            } else if (mEngine.clickIfPresent("fengmo_answer", "option_1")) {
                mEngine.sleep(3000);
            } else {
                // Try clicking common answer positions
                mEngine.clickAt(mEngine.mScreenSize[0] / 2, mEngine.mScreenSize[1] * 2 / 3);
                mEngine.sleep(3000);
            }

            // Wait for result
            mEngine.sleep(2000);
            mEngine.clickIfPresent("common", "ok");
            mEngine.clickIfPresent("common", "next");
            mEngine.sleep(2000);
        }

        mEngine.notifyProgress("Feng Mo Answer completed");
    }

    private boolean navigateToFengMoAnswer() {
        if (!mEngine.waitAndClick("common", "explore", 10000)) return false;
        mEngine.sleep(2000);
        if (!mEngine.waitAndClick("fengmo_answer", "fengmo_quiz_icon", 10000)) return false;
        mEngine.sleep(3000);
        return true;
    }

    @Override
    public String getName() {
        return "逢魔答题";
    }

    @Override
    public String getDescription() {
        return "自动参与逢魔之时答题";
    }

    @Override
    protected String getModule() {
        return "fengmo_answer";
    }
}
