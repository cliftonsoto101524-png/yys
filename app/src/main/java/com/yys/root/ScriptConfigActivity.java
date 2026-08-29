package com.yys.root;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

/**
 * Script configuration activity.
 */
public class ScriptConfigActivity extends AppCompatActivity {

    private String mScriptName;
    private String mDisplayName;
    private ConfigManager mConfig;
    private Map<String, Object> mScriptConfig;

    private TextView tvScriptName;
    private SwitchCompat switchEnabled;
    private LinearLayout layoutConfig;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_config);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mScriptName = getIntent().getStringExtra("script_name");
        mDisplayName = getIntent().getStringExtra("script_display_name");
        mConfig = ConfigManager.getInstance();
        mScriptConfig = mConfig.getScriptConfig(mScriptName);

        initViews();
        buildConfigForm();
    }

    private void initViews() {
        tvScriptName = findViewById(R.id.tv_script_name);
        switchEnabled = findViewById(R.id.switch_enabled);
        layoutConfig = findViewById(R.id.layout_config);
        btnStart = findViewById(R.id.btn_start_script);

        tvScriptName.setText(mDisplayName);
        switchEnabled.setChecked(mConfig.isScriptEnabled(mScriptName));

        switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mConfig.setScriptEnabled(mScriptName, isChecked);
        });

        btnStart.setOnClickListener(v -> {
            saveConfig();
            Intent intent = new Intent(this, ScriptRunnerService.class);
            intent.putExtra("script_name", mScriptName);
            startForegroundService(intent);
            Toast.makeText(this, "脚本已启动: " + mDisplayName, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void buildConfigForm() {
        layoutConfig.removeAllViews();

        // Add common config fields based on script type
        switch (mScriptName) {
            case "huntu":
            case "getu":
            case "liaotu":
            case "yeyuanhuo":
            case "yinjie":
                addSliderConfig("floor", "层数", 1, 12, 11);
                addSliderConfig("count", "次数", 1, 200, 50);
                break;
            case "baigui":
                addSliderConfig("tickets", "票数", 1, 50, 10);
                break;
            case "world_repeat":
                addTextConfig("message", "消息内容", "互助互赞");
                addSliderConfig("interval", "间隔(秒)", 5, 60, 15);
                addSliderConfig("count", "次数", 1, 500, 100);
                break;
            case "hunting":
                addSliderConfig("count", "次数", 1, 20, 10);
                break;
            case "summon":
                addSliderConfig("count", "次数", 1, 100, 50);
                addSwitchConfig("use_ticket", "使用票召唤", true);
                break;
            case "fengmo":
                addSwitchConfig("exploration", "探索地图", true);
                addSwitchConfig("boss", "BOSS战", true);
                break;
            case "tanshigui":
                addSliderConfig("count", "次数", 1, 200, 100);
                break;
            case "fengmo_answer":
                addSliderConfig("count", "答题数", 1, 20, 10);
                break;
            case "jiyang":
                addSwitchConfig("use_taigu", "使用太古", true);
                break;
            case "upselect":
                addTextConfig("targets", "目标式神(逗号分隔)", "sp,ssr");
                break;
            default:
                TextView tv = new TextView(this);
                tv.setText("此脚本无需额外配置");
                tv.setPadding(16, 16, 16, 16);
                layoutConfig.addView(tv);
                break;
        }
    }

    private void addSliderConfig(String key, String label, int min, int max, int defaultVal) {
        View view = getLayoutInflater().inflate(R.layout.item_config_slider, layoutConfig, false);
        TextView tvLabel = view.findViewById(R.id.tv_label);
        Slider slider = view.findViewById(R.id.slider);
        TextView tvValue = view.findViewById(R.id.tv_value);

        tvLabel.setText(label);
        slider.setValueFrom(min);
        slider.setValueTo(max);

        int current = mScriptConfig.containsKey(key) ? ((Number) mScriptConfig.get(key)).intValue() : defaultVal;
        slider.setValue(current);
        tvValue.setText(String.valueOf(current));

        slider.addOnChangeListener((s, value, fromUser) -> {
            tvValue.setText(String.valueOf((int) value));
            mScriptConfig.put(key, (int) value);
        });

        layoutConfig.addView(view);
    }

    private void addTextConfig(String key, String label, String defaultVal) {
        View view = getLayoutInflater().inflate(R.layout.item_config_text, layoutConfig, false);
        TextView tvLabel = view.findViewById(R.id.tv_label);
        TextInputEditText editText = view.findViewById(R.id.edit_text);

        tvLabel.setText(label);
        String current = mScriptConfig.containsKey(key) ? (String) mScriptConfig.get(key) : defaultVal;
        editText.setText(current);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                mScriptConfig.put(key, editText.getText().toString());
            }
        });

        layoutConfig.addView(view);
    }

    private void addSwitchConfig(String key, String label, boolean defaultVal) {
        View view = getLayoutInflater().inflate(R.layout.item_config_switch, layoutConfig, false);
        TextView tvLabel = view.findViewById(R.id.tv_label);
        SwitchCompat switchView = view.findViewById(R.id.switch_view);

        tvLabel.setText(label);
        boolean current = mScriptConfig.containsKey(key) ? (Boolean) mScriptConfig.get(key) : defaultVal;
        switchView.setChecked(current);
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mScriptConfig.put(key, isChecked);
        });

        layoutConfig.addView(view);
    }

    private void saveConfig() {
        mConfig.setScriptConfig(mScriptName, mScriptConfig);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveConfig();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveConfig();
    }
}
