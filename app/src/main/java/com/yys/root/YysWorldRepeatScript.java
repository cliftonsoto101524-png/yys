package com.yys.root;

import java.util.Map;

/**
 * World chat repeat (世界复读) automation script.
 * Repeats messages in world chat.
 */
public class YysWorldRepeatScript extends YysAuto {

    public YysWorldRepeatScript(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void run() {
        mEngine.notifyProgress("Starting world chat repeat...");

        if (!mEngine.isGameRunning()) {
            mEngine.startGame();
            mEngine.sleep(15000);
        }

        Map<String, Object> config = mConfig.getScriptConfig("world_repeat");
        String message = config.containsKey("message") ? (String) config.get("message") : "互助互赞";
        int interval = config.containsKey("interval") ? ((Number) config.get("interval")).intValue() : 15000;
        int repeatCount = config.containsKey("count") ? ((Number) config.get("count")).intValue() : 100;

        // Navigate to world chat
        if (!navigateToWorldChat()) {
            mEngine.notifyProgress("Failed to navigate to world chat");
            return;
        }

        for (int i = 0; i < repeatCount && mEngine.isRunning(); i++) {
            mEngine.notifyProgress("Sending message " + (i + 1) + "/" + repeatCount);
            sendMessage(message);
            mEngine.sleep(interval);
        }

        mEngine.notifyProgress("World repeat completed");
    }

    private boolean navigateToWorldChat() {
        // Open chat from main screen
        if (!mEngine.waitAndClick("common", "chat_button", 10000)) return false;
        mEngine.sleep(2000);
        // Switch to world tab
        if (!mEngine.waitAndClick("world_repeat", "world_tab", 5000)) return false;
        mEngine.sleep(1000);
        return true;
    }

    private void sendMessage(String message) {
        // Click input field
        if (mEngine.waitAndClick("world_repeat", "chat_input", 5000)) {
            mEngine.sleep(500);
            mEngine.inputText(message);
            mEngine.sleep(500);
            mEngine.clickIfPresent("world_repeat", "send_button");
            mEngine.sleep(1000);
        }
    }

    @Override
    public String getName() {
        return "世界复读";
    }

    @Override
    public String getDescription() {
        return "在世界频道自动复读消息";
    }

    @Override
    protected String getModule() {
        return "world_repeat";
    }
}
