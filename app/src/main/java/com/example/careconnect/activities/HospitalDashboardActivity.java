package com.example.careconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.careconnect.firebase.FirebaseAuthManager;
import com.example.careconnect.firebase.FirestoreManager;
import com.example.careconnect.models.Hospital;
import com.example.careconnect.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HospitalDashboardActivity extends AppCompatActivity {

    private TextView tvHospitalName, tvHospitalLocation, tvHospitalPhone, tvStatusLabel, tvLastUpdated;
    private MaterialSwitch switchActive;
    private TextView tvGeneralCount, tvIcuCount, tvEmergencyCount;
    private MaterialButton btnGeneralMinus, btnGeneralPlus, btnIcuMinus, btnIcuPlus, btnEmergencyMinus, btnEmergencyPlus;
    private MaterialButton btnSaveBeds, btnPreviewPublic;
    private ProgressBar pbLoading;
    private TextView tvLogout;

    private String hospitalId;
    private int generalCount = 0, icuCount = 0, emergencyCount = 0;
    private boolean isAcceptingCases = true;
    private ListenerRegistration hospitalListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_dashboard);

        hospitalId = getIntent().getStringExtra(Constants.EXTRA_HOSPITAL_ID);
        if (hospitalId == null || hospitalId.isEmpty()) {
            hospitalId = FirebaseAuthManager.getInstance().getCurrentUserId();
        }

        if (hospitalId == null) {
            Toast.makeText(this, "Session invalid. Please log in.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        initViews();
        setupListeners();
        loadHospitalData();
    }

    private void initViews() {
        tvHospitalName = findViewById(R.id.tv_dash_hosp_name);
        tvHospitalLocation = findViewById(R.id.tv_dash_hosp_location);
        tvHospitalPhone = findViewById(R.id.tv_dash_hosp_phone);
        tvStatusLabel = findViewById(R.id.tv_dash_status_label);
        tvLastUpdated = findViewById(R.id.tv_dash_last_updated);
        switchActive = findViewById(R.id.switch_dash_active);

        tvGeneralCount = findViewById(R.id.tv_dash_general_count);
        tvIcuCount = findViewById(R.id.tv_dash_icu_count);
        tvEmergencyCount = findViewById(R.id.tv_dash_emergency_count);

        btnGeneralMinus = findViewById(R.id.btn_dash_general_minus);
        btnGeneralPlus = findViewById(R.id.btn_dash_general_plus);
        btnIcuMinus = findViewById(R.id.btn_dash_icu_minus);
        btnIcuPlus = findViewById(R.id.btn_dash_icu_plus);
        btnEmergencyMinus = findViewById(R.id.btn_dash_emergency_minus);
        btnEmergencyPlus = findViewById(R.id.btn_dash_emergency_plus);

        btnSaveBeds = findViewById(R.id.btn_dash_save_beds);
        btnPreviewPublic = findViewById(R.id.btn_dash_preview_public);
        pbLoading = findViewById(R.id.pb_dash_loading);
        tvLogout = findViewById(R.id.tv_dash_logout);
    }

    private void setupListeners() {
        btnGeneralMinus.setOnClickListener(v -> { if (generalCount > 0) { generalCount--; updateViews(); } });
        btnGeneralPlus.setOnClickListener(v -> { generalCount++; updateViews(); });

        btnIcuMinus.setOnClickListener(v -> { if (icuCount > 0) { icuCount--; updateViews(); } });
        btnIcuPlus.setOnClickListener(v -> { icuCount++; updateViews(); });

        btnEmergencyMinus.setOnClickListener(v -> { if (emergencyCount > 0) { emergencyCount--; updateViews(); } });
        btnEmergencyPlus.setOnClickListener(v -> { emergencyCount++; updateViews(); });

        switchActive.setOnCheckedChangeListener((btn, isChecked) -> {
            isAcceptingCases = isChecked;
            updateStatusLabel(isChecked);
        });

        btnSaveBeds.setOnClickListener(v -> saveBedUpdates());

        btnPreviewPublic.setOnClickListener(v -> {
            startActivity(new Intent(this, HospitalListActivity.class));
        });

        tvLogout.setOnClickListener(v -> {
            FirebaseAuthManager.getInstance().logout();
            Toast.makeText(this, "Logged out from Hospital Portal", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }

    private void loadHospitalData() {
        setLoading(true);
        hospitalListener = FirestoreManager.getInstance().attachHospitalListener(hospitalId, (snapshot, error) -> {
            setLoading(false);
            if (snapshot != null && snapshot.exists()) {
                Hospital h = snapshot.toObject(Hospital.class);
                if (h != null) {
                    tvHospitalName.setText(h.getName());
                    tvHospitalLocation.setText(h.getCity() + " • " + h.getAddress());
                    tvHospitalPhone.setText("Emergency Hotline: " + h.getContactPhone());
                    generalCount = h.getGeneralBeds();
                    icuCount = h.getIcuBeds();
                    emergencyCount = h.getEmergencyBeds();
                    isAcceptingCases = h.isActive();

                    // -------------------------------------------------------------
                    // Temporarily detach listener while populating state from cloud
                    // to prevent automatic loop re-triggering:
                    // -------------------------------------------------------------
                    switchActive.setOnCheckedChangeListener(null);
                    switchActive.setChecked(isAcceptingCases);
                    updateStatusLabel(isAcceptingCases);
                    switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        isAcceptingCases = isChecked;
                        updateStatusLabel(isChecked);
                    });

                    updateViews();
                }
            }
        });
    }

    private void updateViews() {
        tvGeneralCount.setText(String.valueOf(generalCount));
        tvIcuCount.setText(String.valueOf(icuCount));
        tvEmergencyCount.setText(String.valueOf(emergencyCount));
    }

    private void updateStatusLabel(boolean active) {
        if (active) {
            tvStatusLabel.setText("Accepting Emergency Cases (OPEN)");
            tvStatusLabel.setTextColor(getColor(R.color.status_available));
        } else {
            tvStatusLabel.setText("Emergency Ward Full / Paused");
            tvStatusLabel.setTextColor(getColor(R.color.status_unavailable));
        }
    }

    private void saveBedUpdates() {
        setLoading(true);
        FirestoreManager.getInstance().updateHospitalBeds(
                hospitalId, generalCount, icuCount, emergencyCount, isAcceptingCases,
                task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Live bed counts broadcasted to patients!", Toast.LENGTH_SHORT).show();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
                        tvLastUpdated.setText("Last Synced: " + sdf.format(new Date()));
                    } else {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveBeds.setEnabled(!isLoading);
        btnGeneralMinus.setEnabled(!isLoading);
        btnGeneralPlus.setEnabled(!isLoading);
        btnIcuMinus.setEnabled(!isLoading);
        btnIcuPlus.setEnabled(!isLoading);
        btnEmergencyMinus.setEnabled(!isLoading);
        btnEmergencyPlus.setEnabled(!isLoading);
        switchActive.setEnabled(!isLoading);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hospitalListener != null) hospitalListener.remove();
    }
}