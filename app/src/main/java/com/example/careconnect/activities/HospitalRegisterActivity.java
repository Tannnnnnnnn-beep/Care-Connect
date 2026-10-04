package com.example.careconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.careconnect.models.Hospital;
import com.example.careconnect.models.User;
import com.example.careconnect.utils.Constants;
import com.example.careconnect.utils.ValidationUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HospitalRegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilPhone, tilAddress;
    private TextInputEditText etName, etEmail, etPassword, etPhone, etAddress;
    private TextInputEditText etGeneralBeds, etIcuBeds, etEmergencyBeds;
    private Spinner spinnerCity;
    private MaterialButton btnSubmit;
    private ProgressBar pbLoading;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_register);

        initViews();
        setupCitySpinner();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.til_hosp_reg_name);
        tilEmail = findViewById(R.id.til_hosp_reg_email);
        tilPassword = findViewById(R.id.til_hosp_reg_password);
        tilPhone = findViewById(R.id.til_hosp_reg_phone);
        tilAddress = findViewById(R.id.til_hosp_reg_address);

        etName = findViewById(R.id.et_hosp_reg_name);
        etEmail = findViewById(R.id.et_hosp_reg_email);
        etPassword = findViewById(R.id.et_hosp_reg_password);
        etPhone = findViewById(R.id.et_hosp_reg_phone);
        etAddress = findViewById(R.id.et_hosp_reg_address);

        etGeneralBeds = findViewById(R.id.et_hosp_reg_general);
        etIcuBeds = findViewById(R.id.et_hosp_reg_icu);
        etEmergencyBeds = findViewById(R.id.et_hosp_reg_emergency);

        spinnerCity = findViewById(R.id.spinner_hosp_reg_city);
        btnSubmit = findViewById(R.id.btn_hosp_reg_submit);
        pbLoading = findViewById(R.id.pb_hosp_reg);
        tvGoToLogin = findViewById(R.id.tv_hosp_reg_go_to_login);
    }

    private void setupCitySpinner() {
        List<String> cities = new ArrayList<>(Arrays.asList(Constants.SAMPLE_CITIES));
        if (!cities.isEmpty() && cities.get(0).equalsIgnoreCase("All Cities")) {
            cities.remove(0);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> performHospitalRegistration());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void performHospitalRegistration() {
        final String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        final String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        final String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        final String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        final String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        final String city = spinnerCity.getSelectedItem() != null ? spinnerCity.getSelectedItem().toString() : "Mumbai";

        int genBeds = 20, icuBeds = 5, emerBeds = 4;
        try {
            if (!TextUtils.isEmpty(etGeneralBeds.getText())) genBeds = Math.max(0, Integer.parseInt(etGeneralBeds.getText().toString().trim()));
            if (!TextUtils.isEmpty(etIcuBeds.getText())) icuBeds = Math.max(0, Integer.parseInt(etIcuBeds.getText().toString().trim()));
            if (!TextUtils.isEmpty(etEmergencyBeds.getText())) emerBeds = Math.max(0, Integer.parseInt(etEmergencyBeds.getText().toString().trim()));
        } catch (NumberFormatException ignored) {}

        if (!ValidationUtils.isNotEmpty(name)) {
            tilName.setError("Enter hospital or healthcare center name");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.err_invalid_email));
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.err_short_password));
            return;
        }

        setLoading(true);
        final int finalGen = genBeds, finalIcu = icuBeds, finalEmer = emerBeds;

        FirebaseAuthManager.getInstance().registerWithEmail(email, password, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                final String uid = task.getResult().getUser().getUid();

                // ✅ Exact 10 parameters matching your Hospital model constructor
                Hospital hospital = new Hospital(
                        uid, name, address, city, phone,
                        finalGen, finalIcu, finalEmer,
                        true, System.currentTimeMillis()
                );

                FirestoreManager.getInstance().saveHospital(hospital, hospTask -> {
                    User user = new User(uid, name, email, phone, "N/A", city, false, System.currentTimeMillis());
                    FirestoreManager.getInstance().saveUser(user, null);

                    setLoading(false);
                    Toast.makeText(this, "Hospital registered successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, HospitalDashboardActivity.class);
                    intent.putExtra(Constants.EXTRA_HOSPITAL_ID, uid);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

            } else {
                setLoading(false);
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                Toast.makeText(this, "Failed: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!isLoading);
    }
}