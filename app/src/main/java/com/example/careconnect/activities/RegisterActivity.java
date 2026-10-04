package com.example.careconnect.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.careconnect.utils.ValidationUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilPhone, tilCity;
    private TextInputEditText etName, etEmail, etPassword, etPhone, etCity;
    private Spinner spinnerBloodGroup;
    private MaterialSwitch switchBecomeDonor;
    private MaterialButton btnRegister;
    private ProgressBar pbLoading;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupBloodGroupSpinner();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.til_register_name);
        tilEmail = findViewById(R.id.til_register_email);
        tilPassword = findViewById(R.id.til_register_password);
        tilPhone = findViewById(R.id.til_register_phone);
        tilCity = findViewById(R.id.til_register_city);

        etName = findViewById(R.id.et_register_name);
        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        etPhone = findViewById(R.id.et_register_phone);
        etCity = findViewById(R.id.et_register_city);

        spinnerBloodGroup = findViewById(R.id.spinner_register_blood_group);
        switchBecomeDonor = findViewById(R.id.switch_become_donor);
        btnRegister = findViewById(R.id.btn_register_submit);
        pbLoading = findViewById(R.id.pb_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);
    }

    private void setupBloodGroupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Constants.BLOOD_GROUPS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performRegistration() {
        final String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        final String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        final String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        final String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        final String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        final String bloodGroup = spinnerBloodGroup.getSelectedItem() != null ? spinnerBloodGroup.getSelectedItem().toString() : "A+";
        final boolean isDonor = switchBecomeDonor.isChecked();

        // Clear previous errors
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilPhone.setError(null);
        tilCity.setError(null);

        // Validation
        if (!ValidationUtils.isNotEmpty(name)) {
            tilName.setError(getString(R.string.err_empty_field));
            etName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.err_invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.err_short_password));
            etPassword.requestFocus();
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

        FirebaseAuthManager.getInstance().registerWithEmail(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                    final String uid = task.getResult().getUser().getUid();

                    // Save User Document
                    final User user = new User(uid, name, email, phone, bloodGroup, city, isDonor, System.currentTimeMillis());
                    FirestoreManager.getInstance().saveUser(user, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> userSaveTask) {
                            if (isDonor) {
                                // Save Donor Document
                                Donor donor = new Donor(uid, name, bloodGroup, phone, city, true, System.currentTimeMillis());
                                FirestoreManager.getInstance().saveDonor(donor, null);
                            }

                            setLoading(false);
                            Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    setLoading(false);
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                    Toast.makeText(RegisterActivity.this, "Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        etName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etPhone.setEnabled(!isLoading);
        etCity.setEnabled(!isLoading);
        spinnerBloodGroup.setEnabled(!isLoading);
        switchBecomeDonor.setEnabled(!isLoading);
    }
}
