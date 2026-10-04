package com.example.careconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.careconnect.firebase.FirebaseAuthManager;
import com.example.careconnect.firebase.FirestoreManager;
import com.example.careconnect.utils.Constants;
import com.example.careconnect.utils.ValidationUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar pbLoading;
    private TextView tvGoToRegister, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.til_login_email);
        tilPassword = findViewById(R.id.til_login_password);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login_submit);
        pbLoading = findViewById(R.id.pb_login);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // 🏥 Click listener for the Hospital Portal card on the login screen
        View cardHospital = findViewById(R.id.card_hospital_portal);
        if (cardHospital != null) {
            cardHospital.setOnClickListener(v ->
                    startActivity(new Intent(this, HospitalRegisterActivity.class))
            );
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Clear errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validation
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.err_invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.err_empty_field));
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        FirebaseAuthManager.getInstance().loginWithEmail(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                    final String uid = task.getResult().getUser().getUid();

                    // 🔍 Smart Check: Is this a Hospital Admin or a regular Citizen?
                    FirestoreManager.getInstance().checkIfHospital(uid, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> hospTask) {
                            setLoading(false);
                            if (hospTask.isSuccessful() && hospTask.getResult() != null && hospTask.getResult().exists()) {
                                // 🏥 Route to Hospital Management Portal
                                Toast.makeText(LoginActivity.this, "Welcome to Hospital Management Portal", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, HospitalDashboardActivity.class);
                                intent.putExtra(Constants.EXTRA_HOSPITAL_ID, uid);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                // 👤 Route to Patient / Blood Donor App
                                Toast.makeText(LoginActivity.this, "Welcome to CareConnect", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            finish();
                        }
                    });

                } else {
                    setLoading(false);
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                    Toast.makeText(LoginActivity.this, "Sign-in failed: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void handleForgotPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (ValidationUtils.isValidEmail(email)) {
            FirebaseAuthManager.getInstance().sendPasswordResetEmail(email, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send reset email: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please enter your registered email address first.", Toast.LENGTH_SHORT).show();
            tilEmail.setError("Enter valid email");
            etEmail.requestFocus();
        }
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }
}