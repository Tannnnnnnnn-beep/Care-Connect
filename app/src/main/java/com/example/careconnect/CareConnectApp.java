package com.example.careconnect;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class CareConnectApp extends Application {

    private static final String TAG = "CareConnectApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Safely ensure Firebase is initialized before any activity runs
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "FirebaseApp initialized successfully in Application class.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Safe Firebase initialization note: " + e.getMessage());
        }
    }
}