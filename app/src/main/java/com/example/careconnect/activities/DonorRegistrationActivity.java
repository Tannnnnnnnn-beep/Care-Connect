package com.example.careconnect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.careconnect.firebase.FirebaseAuthManager;
import com.example.careconnect.firebase.FirestoreManager;
import com.example.careconnect.models.Donor;
import com.example.careconnect.models.User;
import com.example.careconnect.utils.Constants;
import com.example.careconnect.utils.ValidationUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

public class DonorRegistrationActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialSwitch switchStatus;
    private TextInputLayout tilName, tilPhone, tilCity;
    private TextInputEditText etName, etPhone, etCity;
    private Spinner spinnerBloodGroup;
    private MaterialButton btnSubmit;
    private ProgressBar pbLoading;

    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_registration);

        currentUid = FirebaseAuthManager.getInstance().getCurrentUserId();

        initViews();
        setupSpinner();
        setupListeners();
        loadExistingDonorData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_donor_reg);
        switchStatus = findViewById(R.id.switch_donor_status_toggle);
        tilName = findViewById(R.id.til_donor_reg_name);
        tilPhone = findViewById(R.id.til_donor_reg_phone);
        tilCity = findViewById(R.id.til_donor_reg_city);
        etName = findViewById(R.id.et_donor_reg_name);
        etPhone = findViewById(R.id.et_donor_reg_phone);
        etCity = findViewById(R.id.et_donor_reg_city);
        spinnerBloodGroup = findViewById(R.id.spinner_donor_reg_blood_group);
        btnSubmit = findViewById(R.id.btn_donor_reg_submit);
        pbLoading = findViewById(R.id.pb_donor_reg);
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

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            switchStatus.setText(isChecked ? "Active & Ready to Donate" : "Temporarily Unavailable to Donate");
            switchStatus.setTextColor(getColor(isChecked ? R.color.status_available : R.color.status_unavailable));
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDonorProfile();
            }
        });
    }

    private void loadExistingDonorData() {
        if (currentUid == null) return;

        setLoading(true);

        // First check donors collection
        FirestoreManager.getInstance().getDonorsCollection().document(currentUid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        setLoading(false);
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            Donor donor = task.getResult().toObject(Donor.class);
                            if (donor != null) {
                                etName.setText(donor.getName());
                                etPhone.setText(donor.getPhone());
                                etCity.setText(donor.getCity());
                                switchStatus.setChecked(donor.isAvailable());

                                setBloodGroupSelection(donor.getBloodGroup());
                                return;
                            }
                        }

                        // Fallback: load info from User collection
                        loadFromUserAccount();
                    }
                });
    }

    private void loadFromUserAccount() {
        if (currentUid == null) return;

        FirestoreManager.getInstance().getUser(currentUid, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    User user = task.getResult().toObject(User.class);
                    if (user != null) {
                        etName.setText(user.getName());
                        etPhone.setText(user.getPhone());
                        etCity.setText(user.getCity());
                        setBloodGroupSelection(user.getBloodGroup());
                    }
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

    private void saveDonorProfile() {
        final String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        final String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        final String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        final String bloodGroup = spinnerBloodGroup.getSelectedItem() != null ? spinnerBloodGroup.getSelectedItem().toString() : "A+";
        final boolean isAvailable = switchStatus.isChecked();

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

        final Donor donor = new Donor(currentUid, currentUid, name, bloodGroup, phone, city, isAvailable, System.currentTimeMillis());

        FirestoreManager.getInstance().saveDonor(donor, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setLoading(false);
                if (task.isSuccessful()) {
                    // Also update isDonor status on User document
                    if (currentUid != null) {
                        FirestoreManager.getInstance().getUsersCollection().document(currentUid).update("donor", true);
                    }
                    Toast.makeText(DonorRegistrationActivity.this, "Donor profile saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Error saving";
                    Toast.makeText(DonorRegistrationActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!isLoading);
        etName.setEnabled(!isLoading);
        etPhone.setEnabled(!isLoading);
        etCity.setEnabled(!isLoading);
        spinnerBloodGroup.setEnabled(!isLoading);
    }
}
