package com.yys.root;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Settings activity for global configuration.
 */
public class SettingsActivity extends AppCompatActivity {

    private ConfigManager mConfig;

    private SwitchCompat switchAutoStart;
    private SwitchCompat switchAntiDetection;
    private SwitchCompat switchAutoReconnect;
    private Slider sliderThreshold;
    private Slider sliderClickDelay;
    private Slider sliderScreenshotQuality;
    private TextInputEditText editUpdateUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mConfig = ConfigManager.getInstance();
        initViews();
        loadSettings();
    }

    private void initViews() {
        switchAutoStart = findViewById(R.id.switch_auto_start);
        switchAntiDetection = findViewById(R.id.switch_anti_detection);
        switchAutoReconnect = findViewById(R.id.switch_auto_reconnect);
        sliderThreshold = findViewById(R.id.slider_threshold);
        sliderClickDelay = findViewById(R.id.slider_click_delay);
        sliderScreenshotQuality = findViewById(R.id.slider_screenshot_quality);
        editUpdateUrl = findViewById(R.id.edit_update_url);

        findViewById(R.id.btn_reset).setOnClickListener(v -> {
            mConfig.resetToDefaults();
            loadSettings();
            Toast.makeText(this, "已恢复默认设置", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSettings() {
        switchAutoStart.setChecked(mConfig.isAutoStart());
        switchAntiDetection.setChecked(mConfig.isAntiDetectionEnabled());
        switchAutoReconnect.setChecked(mConfig.isAutoReconnect());
        sliderThreshold.setValue(mConfig.getMatchThreshold());
        sliderClickDelay.setValue(mConfig.getClickDelay());
        sliderScreenshotQuality.setValue(mConfig.getScreenshotQuality());
        editUpdateUrl.setText(mConfig.getCloudUpdateUrl());
    }

    private void saveSettings() {
        mConfig.setAutoStart(switchAutoStart.isChecked());
        mConfig.setAntiDetectionEnabled(switchAntiDetection.isChecked());
        mConfig.setAutoReconnect(switchAutoReconnect.isChecked());
        mConfig.setMatchThreshold(sliderThreshold.getValue());
        mConfig.setClickDelay((int) sliderClickDelay.getValue());
        mConfig.setScreenshotQuality((int) sliderScreenshotQuality.getValue());
        mConfig.setCloudUpdateUrl(editUpdateUrl.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveSettings();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }
}
