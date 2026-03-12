package com.example.pulsetracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Apply saved theme
        applyTheme();

        initViews();
        setupListeners();
        setupThemeToggle();
    }

    private void setupThemeToggle() {
        MaterialButton btnThemeToggle = findViewById(R.id.btnThemeToggle);
        updateThemeIcon(btnThemeToggle);
        btnThemeToggle.setOnClickListener(v -> {
            sessionManager.setDarkMode(!sessionManager.isDarkMode());
            applyTheme();
            recreate();
        });
    }

    private void applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
                sessionManager.isDarkMode()
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void updateThemeIcon(MaterialButton btn) {
        btn.setIconResource(
                sessionManager.isDarkMode()
                        ? R.drawable.ic_light_mode
                        : R.drawable.ic_dark_mode);
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void attemptRegister() {
        // Clear previous errors
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // --- Validation ---
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }
        if (fullName.length() < 2) {
            tilFullName.setError("Name must be at least 2 characters");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // --- Check duplicate email ---
        if (databaseHelper.isEmailExists(email)) {
            tilEmail.setError("This email is already registered");
            etEmail.requestFocus();
            return;
        }

        // --- Register user ---
        if (databaseHelper.registerUser(fullName, email, password)) {
            User user = databaseHelper.getUserByEmail(email);
            if (user != null) {
                sessionManager.createLoginSession(user.getId(), user.getFullName(), user.getEmail());
            }
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "Registration failed. Please try again.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
