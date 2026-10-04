package com.example.careconnect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.careconnect.firebase.FirebaseAuthManager;
import com.example.careconnect.firebase.FirestoreManager;
import com.example.careconnect.models.Donor;
import com.example.careconnect.models.User;
import com.example.careconnect.utils.Constants;
import com.example.careconnect.utils.EcoModeManager;
import com.example.careconnect.utils.ValidationUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CareConnect_ProfilePrefs";
    private static final String KEY_PREFIX_DONOR = "is_donor_listed_";
    private static final String KEY_PREFIX_AVAIL = "is_donor_available_";

    private TextView tvHeaderName, tvHeaderEmail, tvLearnGreenIT;
    private TextInputLayout tilName, tilPhone, tilCity;
    private TextInputEditText etName, etPhone, etCity;
    private Spinner spinnerBloodGroup;
    private MaterialSwitch switchIsDonor, switchDonorAvailable, switchEcoMode;
    private MaterialButton btnSave, btnLogout, btnViewCertificate;
    private ProgressBar pbLoading;
    private BottomNavigationView bottomNav;

    private EcoModeManager ecoModeManager;
    private SharedPreferences sharedPreferences;
    private String currentUid;
    private User currentUser;

    // Guard flag to prevent switch listeners from firing while loading from DB/cache
    private boolean isBindingData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ecoModeManager = new EcoModeManager(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUid = FirebaseAuthManager.getInstance().getCurrentUserId();

        if (currentUid == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupSpinner();
        setupListeners();

        // 1. Instantly restore cached switch states to prevent flicker or reset
        restoreCachedSwitchStates();

        // 2. Fetch fresh profile data from Firestore
        loadProfileData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
        if (switchEcoMode != null) {
            switchEcoMode.setChecked(ecoModeManager.isEcoModeEnabled());
        }
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tv_profile_header_name);
        tvHeaderEmail = findViewById(R.id.tv_profile_header_email);
        tvLearnGreenIT = findViewById(R.id.tv_profile_learn_green_it);

        tilName = findViewById(R.id.til_profile_name);
        tilPhone = findViewById(R.id.til_profile_phone);
        tilCity = findViewById(R.id.til_profile_city);

        etName = findViewById(R.id.et_profile_name);
        etPhone = findViewById(R.id.et_profile_phone);
        etCity = findViewById(R.id.et_profile_city);

        spinnerBloodGroup = findViewById(R.id.spinner_profile_blood_group);
        switchIsDonor = findViewById(R.id.switch_profile_is_donor);
        switchDonorAvailable = findViewById(R.id.switch_profile_donor_available);
        switchEcoMode = findViewById(R.id.switch_profile_eco_mode);

        btnSave = findViewById(R.id.btn_profile_save);
        btnLogout = findViewById(R.id.btn_profile_logout);
        btnViewCertificate = findViewById(R.id.btn_profile_view_certificate);
        pbLoading = findViewById(R.id.pb_profile_loading);
        bottomNav = findViewById(R.id.bottom_navigation_profile);

        switchEcoMode.setChecked(ecoModeManager.isEcoModeEnabled());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Constants.BLOOD_GROUPS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(adapter);
    }

    private void restoreCachedSwitchStates() {
        if (sharedPreferences == null || currentUid == null) return;

        boolean cachedIsDonor = sharedPreferences.getBoolean(KEY_PREFIX_DONOR + currentUid, false);
        boolean cachedIsAvailable = sharedPreferences.getBoolean(KEY_PREFIX_AVAIL + currentUid, true);

        isBindingData = true;
        switchIsDonor.setChecked(cachedIsDonor);
        switchDonorAvailable.setChecked(cachedIsAvailable);
        updateDonorControlsState(cachedIsDonor);
        isBindingData = false;
    }

    private void setupListeners() {
        // Instant Auto-Save on "Listed in Donor Directory" Toggle
        switchIsDonor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBindingData) return;

            updateDonorControlsState(isChecked);

            // Persist to local cache immediately
            sharedPreferences.edit()
                    .putBoolean(KEY_PREFIX_DONOR + currentUid, isChecked)
                    .apply();

            // Persist to Cloud Firestore immediately
            syncDonorToggleToCloud(isChecked, switchDonorAvailable.isChecked());

            Toast.makeText(ProfileActivity.this,
                    isChecked ? "Listed in Donor Directory" : "Removed from Donor Directory",
                    Toast.LENGTH_SHORT).show();
        });

        // Instant Auto-Save on "Available for Emergencies" Toggle
        switchDonorAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBindingData) return;

            // Persist to local cache immediately
            sharedPreferences.edit()
                    .putBoolean(KEY_PREFIX_AVAIL + currentUid, isChecked)
                    .apply();

            // Persist to Cloud Firestore immediately
            FirestoreManager.getInstance().updateDonorAvailability(currentUid, isChecked, null);

            Toast.makeText(ProfileActivity.this,
                    isChecked ? "Emergency Status: Available" : "Emergency Status: Unavailable",
                    Toast.LENGTH_SHORT).show();
        });

        // Toggle Eco Mode
        switchEcoMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ecoModeManager.setEcoModeEnabled(isChecked);
            Toast.makeText(ProfileActivity.this, isChecked ? "Green IT / Eco Mode enabled" : "Eco Mode disabled", Toast.LENGTH_SHORT).show();
        });

        // Learn Green IT
        tvLearnGreenIT.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, GreenITActivity.class);
            startActivity(intent);
        });

        // View Certificate Button
        if (btnViewCertificate != null) {
            btnViewCertificate.setOnClickListener(v -> {
                if (!switchIsDonor.isChecked()) {
                    Toast.makeText(ProfileActivity.this, "Certificate locked: You must be listed as an active blood donor to generate a certificate.", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(ProfileActivity.this, DigitalCertificateActivity.class);
                startActivity(intent);
            });
        }

        // Save Profile
        btnSave.setOnClickListener(v -> saveProfileChanges());

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuthManager.getInstance().logout();
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        // Bottom Navigation
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_donors) {
                    Intent intent = new Intent(ProfileActivity.this, DonorSearchActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_hospitals) {
                    Intent intent = new Intent(ProfileActivity.this, HospitalListActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });
    }

    private void updateDonorControlsState(boolean isDonor) {
        if (switchDonorAvailable != null) {
            switchDonorAvailable.setEnabled(isDonor);
            switchDonorAvailable.setAlpha(isDonor ? 1.0f : 0.45f);
            if (!isDonor) {
                switchDonorAvailable.setChecked(false);
            }
        }

        if (btnViewCertificate != null) {
            btnViewCertificate.setEnabled(isDonor);
            btnViewCertificate.setAlpha(isDonor ? 1.0f : 0.45f);
            if (isDonor) {
                btnViewCertificate.setText("View Digital Green Certificate");
            } else {
                btnViewCertificate.setText("Certificate Locked (Requires Donor Status)");
            }
        }
    }

    private void syncDonorToggleToCloud(boolean isDonor, boolean isAvailable) {
        String name = etName.getText() != null && !etName.getText().toString().trim().isEmpty()
                ? etName.getText().toString().trim()
                : (currentUser != null && currentUser.getName() != null ? currentUser.getName() : "CareConnect User");

        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : (currentUser != null ? currentUser.getPhone() : "");
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : (currentUser != null ? currentUser.getCity() : "");
        String bloodGroup = spinnerBloodGroup.getSelectedItem() != null ? spinnerBloodGroup.getSelectedItem().toString() : "A+";
        String email = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "";

        // 1. Update User document with donor flags
        User updatedUser = new User(currentUid, name, email, phone, bloodGroup, city, isDonor, System.currentTimeMillis());
        FirestoreManager.getInstance().saveUser(updatedUser, null);

        // 2. Update Donors directory collection
        if (isDonor) {
            Donor donor = new Donor(currentUid, currentUid, name, bloodGroup, phone, city, isAvailable, System.currentTimeMillis());
            FirestoreManager.getInstance().saveDonor(donor, null);
        } else {
            // Update availability to false and delete donor directory entry so it never shows in searches
            FirestoreManager.getInstance().updateDonorAvailability(currentUid, false, null);
            if (FirestoreManager.getInstance().getDonorsCollection() != null) {
                FirestoreManager.getInstance().getDonorsCollection().document(currentUid).delete();
            }
        }
    }

    private void loadProfileData() {
        setLoading(true);

        FirestoreManager.getInstance().getUser(currentUid, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                setLoading(false);
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    currentUser = task.getResult().toObject(User.class);
                    if (currentUser != null) {
                        tvHeaderName.setText(currentUser.getName() != null ? currentUser.getName() : "CareConnect User");
                        tvHeaderEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
                        etName.setText(currentUser.getName());
                        etPhone.setText(currentUser.getPhone());
                        etCity.setText(currentUser.getCity());

                        // Read donor boolean reliably from snapshot
                        Boolean isDonorBool = task.getResult().getBoolean("donor");
                        if (isDonorBool == null) isDonorBool = task.getResult().getBoolean("isDonor");
                        boolean isDonor = (isDonorBool != null) ? isDonorBool : currentUser.isDonor();

                        // Cache in preferences
                        sharedPreferences.edit()
                                .putBoolean(KEY_PREFIX_DONOR + currentUid, isDonor)
                                .apply();

                        // Guard against listener firing
                        isBindingData = true;
                        switchIsDonor.setChecked(isDonor);
                        updateDonorControlsState(isDonor);
                        setBloodGroupSelection(currentUser.getBloodGroup());
                        isBindingData = false;

                        if (isDonor) {
                            loadDonorAvailability();
                        }
                    }
                }
            }
        });
    }

    private void loadDonorAvailability() {
        if (FirestoreManager.getInstance().getDonorsCollection() == null) return;
        FirestoreManager.getInstance().getDonorsCollection().document(currentUid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            Donor donor = task.getResult().toObject(Donor.class);
                            Boolean availBool = task.getResult().getBoolean("available");
                            if (availBool == null) availBool = task.getResult().getBoolean("isAvailable");
                            boolean isAvailable = (availBool != null) ? availBool : (donor != null && donor.isAvailable());

                            sharedPreferences.edit()
                                    .putBoolean(KEY_PREFIX_AVAIL + currentUid, isAvailable)
                                    .apply();

                            isBindingData = true;
                            if (switchDonorAvailable != null) {
                                switchDonorAvailable.setChecked(isAvailable);
                            }
                            isBindingData = false;
                        }
                    }
                });
    }

    private void setBloodGroupSelection(String bloodGroup) {
        if (bloodGroup == null) return;
        for (int i = 0; i < Constants.BLOOD_GROUPS.length; i++) {
            if (Constants.BLOOD_GROUPS[i].equalsIgnoreCase(bloodGroup)) {
                spinnerBloodGroup.setSelection(i);
                break;
            }
        }
    }

    private void saveProfileChanges() {
        final String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        final String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        final String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        final String bloodGroup = spinnerBloodGroup.getSelectedItem() != null ? spinnerBloodGroup.getSelectedItem().toString() : "A+";
        final boolean isDonor = switchIsDonor.isChecked();
        final boolean isAvailable = switchDonorAvailable.isChecked();

        tilName.setError(null);
        tilPhone.setError(null);
        tilCity.setError(null);

        if (!ValidationUtils.isNotEmpty(name)) {
            tilName.setError(getString(R.string.err_empty_field));
            etName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPhone(phone)) {
            tilPhone.setError(getString(R.string.err_invalid_phone));
            etPhone.requestFocus();
            return;
        }

        if (!ValidationUtils.isNotEmpty(city)) {
            tilCity.setError(getString(R.string.err_empty_field));
            etCity.requestFocus();
            return;
        }

        setLoading(true);

        String email = (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "";
        final User updatedUser = new User(currentUid, name, email, phone, bloodGroup, city, isDonor, System.currentTimeMillis());

        // Save to preferences
        sharedPreferences.edit()
                .putBoolean(KEY_PREFIX_DONOR + currentUid, isDonor)
                .putBoolean(KEY_PREFIX_AVAIL + currentUid, isAvailable)
                .apply();

        FirestoreManager.getInstance().saveUser(updatedUser, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (isDonor) {
                    Donor donor = new Donor(currentUid, currentUid, name, bloodGroup, phone, city, isAvailable, System.currentTimeMillis());
                    FirestoreManager.getInstance().saveDonor(donor, null);
                } else {
                    FirestoreManager.getInstance().updateDonorAvailability(currentUid, false, null);
                    if (FirestoreManager.getInstance().getDonorsCollection() != null) {
                        FirestoreManager.getInstance().getDonorsCollection().document(currentUid).delete();
                    }
                }

                setLoading(false);
                updateDonorControlsState(isDonor);
                tvHeaderName.setText(name);
                Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        btnLogout.setEnabled(!isLoading);
        etName.setEnabled(!isLoading);
        etPhone.setEnabled(!isLoading);
        etCity.setEnabled(!isLoading);
        spinnerBloodGroup.setEnabled(!isLoading);
        switchIsDonor.setEnabled(!isLoading);
        if (switchIsDonor.isChecked()) {
            switchDonorAvailable.setEnabled(!isLoading);
        }
    }
}