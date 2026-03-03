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

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Apply saved theme
        applyTheme();

        // If already logged in, go straight to main
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

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
        });
    }

    private void applyTheme() {
        if (sessionManager.hasDarkModePref()) {
            AppCompatDelegate.setDefaultNightMode(
                    sessionManager.isDarkMode()
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void updateThemeIcon(MaterialButton btn) {
        btn.setIconResource(
                sessionManager.isDarkMode()
                        ? R.drawable.ic_light_mode
                        : R.drawable.ic_dark_mode);
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        tvForgotPassword.setOnClickListener(v ->
                Snackbar.make(findViewById(android.R.id.content),
                        "Password recovery coming soon!", Snackbar.LENGTH_SHORT).show());
    }

    private void attemptLogin() {
        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validate user input before authentication
       

        // --- Validation ---
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

        // --- Authenticate ---
        if (databaseHelper.authenticateUser(email, password)) {
            User user = databaseHelper.getUserByEmail(email);
            if (user != null) {
                sessionManager.createLoginSession(user.getId(), user.getFullName(), user.getEmail());
                Toast.makeText(this, "Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "Invalid email or password", Snackbar.LENGTH_LONG).show();
            tilPassword.setError(" ");
            tilEmail.setError(" ");
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
