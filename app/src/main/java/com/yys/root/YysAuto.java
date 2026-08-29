package com.yys.root;

import java.util.HashMap;
import java.util.Map;

/**
 * Base automation script collection for YYS (Onmyoji).
 * Provides common game automation primitives and script registry.
 */
public abstract class YysAuto {

    protected final ScriptEngine mEngine;
    protected final ConfigManager mConfig;
    protected final TemplateManager mTemplates;
    protected final AntiDetection mAntiDetection;

    // Script registry
    private static final Map<String, Class<? extends YysAuto>> sScriptRegistry = new HashMap<>();

    static {
        registerScripts();
    }

    private static void registerScripts() {
        sScriptRegistry.put("daily", YysDailyScript.class);
        sScriptRegistry.put("huntu", YysHunTuScript.class);
        sScriptRegistry.put("baigui", YysBaiGuiScript.class);
        sScriptRegistry.put("getu", YysGeTuScript.class);
        sScriptRegistry.put("liaotu", YysLiaoTuScript.class);
        sScriptRegistry.put("yeyuanhuo", YysYeYuanHuoScript.class);
        sScriptRegistry.put("world_repeat", YysWorldRepeatScript.class);
        sScriptRegistry.put("hunting", YysHuntingScript.class);
        sScriptRegistry.put("xuanshang", YysXuanShangScript.class);
        sScriptRegistry.put("jiyang", YysJiYangScript.class);
        sScriptRegistry.put("xiaozhiren", YysXiaoZhiRenScript.class);
        sScriptRegistry.put("yinjie", YysYinJieScript.class);
        sScriptRegistry.put("fengmo", YysFengMoScript.class);
        sScriptRegistry.put("summon", YysSummonScript.class);
        sScriptRegistry.put("tanshigui", YysTanShiGuiScript.class);
        sScriptRegistry.put("upselect", YysUpSelectScript.class);
        sScriptRegistry.put("fengmo_answer", YysFengMoAnswerScript.class);
        sScriptRegistry.put("reconnect", YysReconnectScript.class);
    }

    public YysAuto(ScriptEngine engine) {
        mEngine = engine;
        mConfig = ConfigManager.getInstance();
        mTemplates = TemplateManager.getInstance();
        mAntiDetection = new AntiDetection();
    }

    /**
     * Main entry point for the script.
     */
    public abstract void run();

    /**
     * Get display name of the script.
     */
    public abstract String getName();

    /**
     * Get description of what the script does.
     */
    public abstract String getDescription();

    /**
     * Get the module name for template lookup.
     */
    protected abstract String getModule();

    /**
     * Create a script instance by name.
     */
    public static YysAuto create(String scriptName, ScriptEngine engine) {
        Class<? extends YysAuto> clazz = sScriptRegistry.get(scriptName);
        if (clazz != null) {
            try {
                return clazz.getConstructor(ScriptEngine.class).newInstance(engine);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Get all registered script names.
     */
    public static String[] getScriptNames() {
        return sScriptRegistry.keySet().toArray(new String[0]);
    }

    /**
     * Check if a script exists.
     */
    public static boolean hasScript(String scriptName) {
        return sScriptRegistry.containsKey(scriptName);
    }

    /**
     * Get script class by name.
     */
    public static Class<? extends YysAuto> getScriptClass(String scriptName) {
        return sScriptRegistry.get(scriptName);
    }

    // --- Common helper methods ---

    /**
     * Navigate to a specific game screen by template sequence.
     */
    protected boolean navigateTo(String... templateSequence) {
        for (String template : templateSequence) {
            mEngine.notifyProgress("Navigating: " + template);
            if (!mEngine.waitAndClick(getModule(), template, 15000)) {
                // Try with common module fallback
                if (!mEngine.waitAndClick("common", template, 5000)) {
                    return false;
                }
            }
            mEngine.sleep(1500);
        }
        return true;
    }

    /**
     * Wait for battle to complete.
     */
    protected boolean waitForBattleEnd(int timeoutMs) {
        mEngine.notifyProgress("Waiting for battle end...");
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs && mEngine.isRunning()) {
            mEngine.checkPause();
            // Check for victory/defeat indicators
            if (mEngine.clickIfPresent(getModule(), "victory")) {
                mEngine.sleep(2000);
                return true;
            }
            if (mEngine.clickIfPresent("common", "victory_ok")) {
                mEngine.sleep(1000);
                return true;
            }
            // Check for defeat
            if (mEngine.anyTemplatePresent(getModule(), "defeat", "defeat_ok")) {
                mEngine.clickIfPresent(getModule(), "defeat_ok");
                mEngine.sleep(1000);
                return false;
            }
            mEngine.sleep(2000);
        }
        return false;
    }

    /**
     * Accept rewards after battle.
     */
    protected void acceptRewards() {
        mEngine.notifyProgress("Accepting rewards...");
        for (int i = 0; i < 5; i++) {
            if (mEngine.clickIfPresent("common", "reward_ok")) {
                mEngine.sleep(1000);
            } else if (mEngine.clickIfPresent("common", "close")) {
                mEngine.sleep(800);
            } else {
                break;
            }
        }
    }

    /**
     * Handle common popup dialogs.
     */
    protected void handlePopups() {
        String[] popupButtons = {"ok", "confirm", "close", "cancel", "skip"};
        for (String btn : popupButtons) {
            if (mEngine.clickIfPresent("common", btn)) {
                mEngine.sleep(800);
                break;
            }
        }
    }

    /**
     * Check and handle energy/runs limit.
     */
    protected boolean checkResourceLimit() {
        // Check for no energy / no tickets indicators
        if (mEngine.anyTemplatePresent(getModule(), "no_energy", "no_ticket")) {
            mEngine.notifyProgress("Resource limit reached, stopping");
            return false;
        }
        return true;
    }

    /**
     * Wait for loading screen to finish.
     */
    protected void waitForLoading(int timeoutMs) {
        mEngine.notifyProgress("Waiting for loading...");
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs && mEngine.isRunning()) {
            mEngine.checkPause();
            // If we can find normal UI elements, loading is done
            if (mEngine.anyTemplatePresent("common", "back", "menu", "home")) {
                return;
            }
            mEngine.sleep(1000);
        }
    }

    /**
     * Enter a dungeon/battle by count.
     */
    protected boolean enterBattle(int count) {
        for (int i = 0; i < count && mEngine.isRunning(); i++) {
            mEngine.notifyProgress("Entering battle " + (i + 1) + "/" + count);
            if (!mEngine.waitAndClick(getModule(), "start_battle", 10000)) {
                return false;
            }
            mEngine.sleep(2000);

            // Wait for battle to end
            boolean victory = waitForBattleEnd(300000); // 5 min timeout
            acceptRewards();
            handlePopups();

            if (!victory) {
                mEngine.notifyProgress("Battle failed, continuing...");
            }

            if (!checkResourceLimit()) {
                return false;
            }

            mAntiDetection.maybeDoRandomAction(
                    mEngine.mShell, mEngine.mScreenSize[0], mEngine.mScreenSize[1]);
        }
        return true;
    }

    /**
     * Enter battle in loop until stopped or resource exhausted.
     */
    protected void enterBattleLoop() {
        int count = 0;
        while (mEngine.isRunning()) {
            count++;
            mEngine.notifyProgress("Battle #" + count);
            if (!mEngine.waitAndClick(getModule(), "start_battle", 10000)) {
                // Maybe already in battle or different state
                mEngine.sleep(3000);
                continue;
            }
            mEngine.sleep(2000);

            boolean victory = waitForBattleEnd(300000);
            acceptRewards();
            handlePopups();

            if (!victory) {
                mEngine.notifyProgress("Battle failed, retrying...");
            }

            if (!checkResourceLimit()) {
                mEngine.notifyProgress("Resource exhausted, stopping");
                break;
            }

            mAntiDetection.maybeDoRandomAction(
                    mEngine.mShell, mEngine.mScreenSize[0], mEngine.mScreenSize[1]);
        }
    }
}
