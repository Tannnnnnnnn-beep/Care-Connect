package com.example.careconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.careconnect.firebase.FirebaseAuthManager;
import com.example.careconnect.firebase.FirestoreManager;
import com.google.firebase.FirebaseApp;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Safe Firebase initialization on startup
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Firebase startup note: " + t.getMessage());
        }

        // Safe layout inflation
        try {
            setContentView(R.layout.activity_splash);
        } catch (Throwable t) {
            Log.e(TAG, "Layout fallback: " + t.getMessage());
            navigateToLogin();
            return;
        }

        // Preload sample hospitals in Firestore if database is empty
        try {
            FirestoreManager.getInstance().seedInitialHospitalsIfEmpty();
        } catch (Throwable ignored) {}

        // Safe delay to Login/Main screen
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;

                try {
                    if (FirebaseAuthManager.getInstance().isUserLoggedIn()) {
                        String uid = FirebaseAuthManager.getInstance().getCurrentUserId();
                        if (uid != null) {
                            try {
                                FirestoreManager.getInstance().checkIfHospital(uid, task -> {
                                    if (!isFinishing()) {
                                        if (task != null && task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                            Intent intent = new Intent(SplashActivity.this, HospitalDashboardActivity.class);
                                            intent.putExtra(com.example.careconnect.utils.Constants.EXTRA_HOSPITAL_ID, uid);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                        finish();
                                    }
                                });
                                return;
                            } catch (Throwable t) {
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                finish();
                                return;
                            }
                        }
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } else {
                        navigateToLogin();
                    }
                } catch (Throwable t) {
                    navigateToLogin();
                }
                finish();
            }
        }, SPLASH_DELAY_MS);
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } catch (Throwable ignored) {}
    }
}