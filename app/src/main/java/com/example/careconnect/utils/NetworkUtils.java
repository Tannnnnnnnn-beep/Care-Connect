package com.example.careconnect.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public final class NetworkUtils {

    private NetworkUtils() {
        // Prevent instantiation
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } catch (Exception e) {
            return false;
        }
    }
}
