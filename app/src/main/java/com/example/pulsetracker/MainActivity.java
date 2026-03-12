package com.example.pulsetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        // Apply saved theme preference
        applyTheme();

        // Redirect to login if not authenticated
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Display user info
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Hello, " + sessionManager.getFullName() + "!");

        // Display today's date
        TextView tvTodayDate = findViewById(R.id.tvTodayDate);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        tvTodayDate.setText(sdf.format(new Date()));

        // Load and display stats
        loadStats();

        // Navigation buttons
        MaterialButton btnAddWorkout = findViewById(R.id.btnAddWorkout);
        btnAddWorkout.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddWorkoutActivity.class));
        });

        MaterialButton btnViewHistory = findViewById(R.id.btnViewHistory);
        btnViewHistory.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, WorkoutHistoryActivity.class));
        });

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Theme toggle button
        MaterialButton btnThemeToggle = findViewById(R.id.btnThemeToggle);
        updateThemeIcon(btnThemeToggle);
        btnThemeToggle.setOnClickListener(v -> {
            boolean isDark = sessionManager.isDarkMode();
            sessionManager.setDarkMode(!isDark);
            applyTheme();
            recreate();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isLoggedIn()) {
            loadStats();
        }
    }

    private void loadStats() {
        int userId = sessionManager.getUserId();
        List<Workout> workouts = databaseHelper.getWorkoutsByUser(userId);

        int totalWorkouts = workouts.size();
        int totalCalories = 0;
        int totalDuration = 0;
        for (Workout w : workouts) {
            totalCalories += w.getCalories();
            totalDuration += w.getDuration();
        }

        TextView tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        TextView tvTotalCalories = findViewById(R.id.tvTotalCalories);
        TextView tvTotalDuration = findViewById(R.id.tvTotalDuration);

        tvTotalWorkouts.setText(String.valueOf(totalWorkouts));
        tvTotalCalories.setText(String.valueOf(totalCalories));
        tvTotalDuration.setText(String.valueOf(totalDuration));
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
}