package com.example.pulsetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Apply saved theme preference
        applyTheme();

        // Redirect to login if not authenticated
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Display user info
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvEmail = findViewById(R.id.tvEmail);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("Welcome, " + sessionManager.getFullName() + "!");
        tvEmail.setText(sessionManager.getEmail());

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        // Theme toggle button
        MaterialButton btnThemeToggle = findViewById(R.id.btnThemeToggle);
        updateThemeIcon(btnThemeToggle);
        btnThemeToggle.setOnClickListener(v -> {
            boolean isDark = sessionManager.isDarkMode();
            sessionManager.setDarkMode(!isDark);
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
}