package com.yys.root;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Screenshot template manager.
 * Manages template images organized by module.
 */
public class TemplateManager {

    private static final String TAG = "TemplateManager";
    private static final String TEMPLATE_DIR = "YysTemplates";

    private static TemplateManager sInstance;
    private File mTemplateDir;
    private final Map<String, List<String>> mModuleTemplates = new HashMap<>();

    private TemplateManager() {}

    public static synchronized TemplateManager getInstance() {
        if (sInstance == null) {
            sInstance = new TemplateManager();
        }
        return sInstance;
    }

    void init(Context context) {
        mTemplateDir = new File(Environment.getExternalStorageDirectory(), TEMPLATE_DIR);
        if (!mTemplateDir.exists()) {
            boolean created = mTemplateDir.mkdirs();
            Log.i(TAG, "Template dir created: " + created);
        }
        scanTemplates();
    }

    /**
     * Scan all template files organized by subdirectories.
     */
    public void scanTemplates() {
        mModuleTemplates.clear();
        if (mTemplateDir == null || !mTemplateDir.exists()) return;

        File[] dirs = mTemplateDir.listFiles(File::isDirectory);
        if (dirs == null) return;

        for (File dir : dirs) {
            String module = dir.getName();
            File[] pngs = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
            List<String> paths = new ArrayList<>();
            if (pngs != null) {
                for (File png : pngs) {
                    paths.add(png.getAbsolutePath());
                }
            }
            mModuleTemplates.put(module, paths);
        }
    }

    /**
     * Get template path by module and template name.
     */
    public String getTemplatePath(String module, String templateName) {
        File moduleDir = new File(mTemplateDir, module);
        File template = new File(moduleDir, templateName + ".png");
        if (template.exists()) {
            return template.getAbsolutePath();
        }
        // Try without extension
        template = new File(moduleDir, templateName);
        if (template.exists()) {
            return template.getAbsolutePath();
        }
        return null;
    }

    /**
     * Get all templates for a module.
     */
    public List<String> getModuleTemplates(String module) {
        List<String> list = mModuleTemplates.get(module);
        return list != null ? list : new ArrayList<>();
    }

    /**
     * Get all available modules.
     */
    public List<String> getModules() {
        return new ArrayList<>(mModuleTemplates.keySet());
    }

    /**
     * Get template directory for a module, creating if needed.
     */
    public File getModuleDir(String module) {
        File dir = new File(mTemplateDir, module);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public File getTemplateDir() {
        return mTemplateDir;
    }

    /**
     * Check if a specific template exists.
     */
    public boolean hasTemplate(String module, String templateName) {
        return getTemplatePath(module, templateName) != null;
    }

    /**
     * Delete a template.
     */
    public boolean deleteTemplate(String module, String templateName) {
        String path = getTemplatePath(module, templateName);
        if (path != null) {
            boolean deleted = new File(path).delete();
            if (deleted) scanTemplates();
            return deleted;
        }
        return false;
    }

    /**
     * Get template count for a module.
     */
    public int getTemplateCount(String module) {
        List<String> list = mModuleTemplates.get(module);
        return list != null ? list.size() : 0;
    }

    /**
     * Get total template count.
     */
    public int getTotalTemplateCount() {
        int count = 0;
        for (List<String> list : mModuleTemplates.values()) {
            count += list.size();
        }
        return count;
    }
}
