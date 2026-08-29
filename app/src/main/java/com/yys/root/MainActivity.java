package com.yys.root;

import android.Manifest;

import java.io.File;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity with beautified UI and script selection grid.
 */
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int OVERLAY_REQUEST_CODE = 101;

    private TextView tvStatus;
    private TextView tvVersion;
    private RecyclerView recyclerScripts;
    private ScriptAdapter mAdapter;
    private ConfigManager mConfig;
    private CloudUpdateManager mUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mConfig = ConfigManager.getInstance();
        mUpdateManager = new CloudUpdateManager();

        initViews();
        checkPermissions();
        checkRoot();
        checkForUpdates();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvVersion = findViewById(R.id.tv_version);
        recyclerScripts = findViewById(R.id.recycler_scripts);

        tvVersion.setText("v" + BuildConfig.VERSION_NAME);
        updateStatus();

        // Setup script grid
        recyclerScripts.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = new ScriptAdapter(getScriptItems());
        mAdapter.setOnItemClickListener(this::onScriptSelected);
        recyclerScripts.setAdapter(mAdapter);

        // Floating action button for float window
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, FloatService.class));
                Snackbar.make(v, "悬浮窗已启动", Snackbar.LENGTH_SHORT).show();
            } else {
                requestOverlayPermission();
            }
        });

        // Quick action cards
        CardView cardDaily = findViewById(R.id.card_daily);
        cardDaily.setOnClickListener(v -> runScriptDirectly("daily"));

        CardView cardHuntu = findViewById(R.id.card_huntu);
        cardHuntu.setOnClickListener(v -> runScriptDirectly("huntu"));

        CardView cardReconnect = findViewById(R.id.card_reconnect);
        cardReconnect.setOnClickListener(v -> runScriptDirectly("reconnect"));
    }

    private List<ScriptItem> getScriptItems() {
        List<ScriptItem> items = new ArrayList<>();
        String[] names = YysAuto.getScriptNames();
        for (String name : names) {
            YysAuto script = YysAuto.create(name, null);
            if (script != null) {
                items.add(new ScriptItem(name, script.getName(), script.getDescription()));
            }
        }
        return items;
    }

    private void onScriptSelected(ScriptItem item) {
        Intent intent = new Intent(this, ScriptConfigActivity.class);
        intent.putExtra("script_name", item.id);
        intent.putExtra("script_display_name", item.displayName);
        startActivity(intent);
    }

    private void runScriptDirectly(String scriptName) {
        Intent intent = new Intent(this, ScriptRunnerService.class);
        intent.putExtra("script_name", scriptName);
        startForegroundService(intent);
        Toast.makeText(this, "启动脚本: " + scriptName, Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    private void updateStatus() {
        // Update status display based on running state
        tvStatus.setText("就绪");
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
    }

    private void checkRoot() {
        boolean hasRoot = RootShell.getInstance().init();
        if (!hasRoot) {
            new AlertDialog.Builder(this)
                    .setTitle("需要Root权限")
                    .setMessage("本应用需要Root权限才能正常工作，请确保设备已Root。")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

    private void checkForUpdates() {
        mUpdateManager.setListener(new CloudUpdateManager.OnUpdateListener() {
            @Override
            public void onCheckStart() {}

            @Override
            public void onUpdateAvailable(String newVersion, String changelog, long fileSize) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("发现新版本: " + newVersion)
                        .setMessage(changelog)
                        .setPositiveButton("下载", (d, w) -> {
                            mUpdateManager.downloadUpdate(newVersion, getExternalCacheDir());
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }

            @Override
            public void onNoUpdate() {}

            @Override
            public void onDownloadProgress(long downloaded, long total) {}

            @Override
            public void onDownloadComplete(File apkFile) {
                Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {}
        });

        // Check once per day
        long lastCheck = mConfig.getLastCheckTime();
        if (System.currentTimeMillis() - lastCheck > 24 * 60 * 60 * 1000) {
            mUpdateManager.checkForUpdate();
            mConfig.setLastCheckTime(System.currentTimeMillis());
        }
    }

    private void requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, FloatService.class));
            } else {
                Toast.makeText(this, "需要悬浮窗权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "需要存储权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_templates) {
            startActivity(new Intent(this, TemplateManagerActivity.class));
            return true;
        } else if (id == R.id.action_check_update) {
            mUpdateManager.checkForUpdate();
            Toast.makeText(this, "检查更新中...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        mAdapter.updateItems(getScriptItems());
    }

    /**
     * Script item data class.
     */
    public static class ScriptItem {
        public final String id;
        public final String displayName;
        public final String description;

        public ScriptItem(String id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }
    }
}
