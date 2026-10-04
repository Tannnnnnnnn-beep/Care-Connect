package com.example.careconnect.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.R;
import com.example.careconnect.utils.BatteryHelper;
import com.example.careconnect.utils.EcoModeManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class GreenITActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialSwitch switchToggle, switchOledDark, switchBatteryThrottle;
    private TextView tvStatusTitle, tvStatusDesc, tvBatteryStatus;
    private EcoModeManager ecoModeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_green_it);

        ecoModeManager = new EcoModeManager(this);

        initViews();
        setupListeners();
        updateStatusDisplay(ecoModeManager.isEcoModeEnabled());
        updateBatteryInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_green_it);
        switchToggle = findViewById(R.id.switch_green_it_toggle);
        switchOledDark = findViewById(R.id.switch_oled_dark_mode);
        switchBatteryThrottle = findViewById(R.id.switch_battery_throttle);
        tvStatusTitle = findViewById(R.id.tv_green_it_status_title);
        tvStatusDesc = findViewById(R.id.tv_green_it_status_desc);
        tvBatteryStatus = findViewById(R.id.tv_battery_status_indicator);

        switchToggle.setChecked(ecoModeManager.isEcoModeEnabled());
        switchOledDark.setChecked(ecoModeManager.isOledPureBlackEnabled());
        switchBatteryThrottle.setChecked(ecoModeManager.isBatteryThrottleEnabled());
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ecoModeManager.setEcoModeEnabled(isChecked);
            updateStatusDisplay(isChecked);
            Toast.makeText(GreenITActivity.this,
                    isChecked ? "Eco Mode Activated: Maximum Battery & Data Conservation" : "Standard Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });

        switchOledDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ecoModeManager.setOledPureBlackEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            );
            Toast.makeText(GreenITActivity.this,
                    isChecked ? "OLED Pure Black Active: Pixels extinguished to save ~40% battery" : "Standard Theme Restored",
                    Toast.LENGTH_SHORT).show();
        });

        switchBatteryThrottle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ecoModeManager.setBatteryThrottleEnabled(isChecked);
            updateBatteryInfo();
            Toast.makeText(GreenITActivity.this,
                    isChecked ? "Ultra-Low Battery Guard Enabled (< 15% auto-throttle)" : "Battery Guard Disabled",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void updateBatteryInfo() {
        if (tvBatteryStatus == null) return;
        int pct = BatteryHelper.getBatteryPercentage(this);
        boolean isCharging = BatteryHelper.isCharging(this);
        boolean isLow = BatteryHelper.isLowBattery(this);

        String chargeText = isCharging ? "⚡ Charging" : "Discharging";
        String statusText = isLow ? "⚠️ LOW (< 15%) - Auto-throttling active" : "Normal";

        tvBatteryStatus.setText("🔋 Device Battery: " + pct + "% (" + chargeText + ") • Status: " + statusText);
        if (isLow) {
            tvBatteryStatus.setTextColor(getColor(R.color.primary));
        } else {
            tvBatteryStatus.setTextColor(getColor(R.color.text_secondary));
        }
    }

    private void updateStatusDisplay(boolean isEnabled) {
        if (isEnabled) {
            tvStatusTitle.setText("Green Computing Active");
            tvStatusDesc.setText("Aggressive offline cache & zero background polling enabled.");
            tvStatusTitle.setTextColor(getColor(R.color.eco_green_dark));
        } else {
            tvStatusTitle.setText("Standard Mode (Eco Off)");
            tvStatusDesc.setText("Real-time network queries enabled without aggressive caching.");
            tvStatusTitle.setTextColor(getColor(R.color.text_secondary));
        }
    }
}