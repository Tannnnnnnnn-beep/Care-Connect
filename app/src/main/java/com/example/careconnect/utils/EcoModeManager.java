package com.example.careconnect.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class EcoModeManager {

    private final Context context;
    private final SharedPreferences preferences;

    public EcoModeManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(
                Constants.PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public boolean isEcoModeEnabled() {
        return preferences.getBoolean(Constants.KEY_ECO_MODE, false);
    }

    public void setEcoModeEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.KEY_ECO_MODE, enabled).apply();
    }

    // Feature 2: OLED Pure Black Theme
    public boolean isOledPureBlackEnabled() {
        return preferences.getBoolean(Constants.KEY_OLED_DARK, false);
    }

    public void setOledPureBlackEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.KEY_OLED_DARK, enabled).apply();
    }

    // Feature 6: Ultra-Low Battery Throttling
    public boolean isBatteryThrottleEnabled() {
        return preferences.getBoolean(Constants.KEY_BATTERY_THROTTLE, true);
    }

    public void setBatteryThrottleEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.KEY_BATTERY_THROTTLE, enabled).apply();
    }

    public boolean isEffectiveEcoMode() {
        if (isEcoModeEnabled()) return true;
        if (isBatteryThrottleEnabled() && BatteryHelper.isLowBattery(context)) {
            return true;
        }
        return false;
    }
}