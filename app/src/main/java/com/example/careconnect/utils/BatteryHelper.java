package com.example.careconnect.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public final class BatteryHelper {

    private BatteryHelper() {}

    public static int getBatteryPercentage(Context context) {
        if (context == null) return 100;
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus == null) return 100;

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) {
                return (int) ((level / (float) scale) * 100);
            }
        } catch (Exception ignored) {}
        return 100;
    }

    public static boolean isCharging(Context context) {
        if (context == null) return false;
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus == null) return false;

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean isLowBattery(Context context) {
        return getBatteryPercentage(context) <= 15 && !isCharging(context);
    }
}