package com.example.pulsetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpdateWorkoutActivity extends AppCompatActivity {

    public static final String WORKOUT_ID_EXTRA = "workout_id";

    private TextInputLayout tilWorkoutType, tilWorkoutDate, tilDuration, tilCalories, tilNotes;
    private AutoCompleteTextView etWorkoutType;
    private TextInputEditText etWorkoutDate, etDuration, etCalories, etNotes;
    private MaterialButton btnUpdateWorkout, btnDeleteWorkout, btnCancel, btnBack, btnThemeToggle;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private Calendar selectedDate;
    private Workout currentWorkout;
    private int workoutId;

    private static final String[] WORKOUT_TYPES = {"Running", "Cycling", "Swimming", "Gym", "Yoga", "Walking", "Hiking", "Sports", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Get workout ID from intent
        workoutId = getIntent().getIntExtra(WORKOUT_ID_EXTRA, -1);
        if (workoutId == -1) {
            Toast.makeText(this, "Invalid workout ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load workout data
        currentWorkout = databaseHelper.getWorkoutById(workoutId);
        if (currentWorkout == null) {
            Toast.makeText(this, "Workout not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        applyTheme();

        EdgeToEdge.enable(this);
        setContentView(R.layout.update_workout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        populateWorkoutData();
        setupWorkoutTypeDropdown();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        tilWorkoutType = findViewById(R.id.tilWorkoutType);
        tilWorkoutDate = findViewById(R.id.tilWorkoutDate);
        tilDuration = findViewById(R.id.tilDuration);
        tilCalories = findViewById(R.id.tilCalories);
        tilNotes = findViewById(R.id.tilNotes);

        etWorkoutType = findViewById(R.id.etWorkoutType);
        etWorkoutDate = findViewById(R.id.etWorkoutDate);
        etDuration = findViewById(R.id.etDuration);
        etCalories = findViewById(R.id.etCalories);
        etNotes = findViewById(R.id.etNotes);

        btnUpdateWorkout = findViewById(R.id.btnUpdateWorkout);
        btnDeleteWorkout = findViewById(R.id.btnDeleteWorkout);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);

        updateThemeIcon(btnThemeToggle);
    }

    private void populateWorkoutData() {
        etWorkoutType.setText(currentWorkout.getWorkoutType(), false);
        etDuration.setText(String.valueOf(currentWorkout.getDuration()));
        etCalories.setText(String.valueOf(currentWorkout.getCalories()));
        etNotes.setText(currentWorkout.getNotes() != null ? currentWorkout.getNotes() : "");

        // Parse and set the date
        selectedDate = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            selectedDate.setTime(sdf.parse(currentWorkout.getWorkoutDate()));
            updateDateDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupWorkoutTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                WORKOUT_TYPES
        );
        etWorkoutType.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etWorkoutDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    updateDateDisplay();
                }, year, month, day);

        // Prevent future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etWorkoutDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnThemeToggle.setOnClickListener(v -> {
            boolean isDark = sessionManager.isDarkMode();
            sessionManager.setDarkMode(!isDark);
            applyTheme();
            recreate();
        });

        btnCancel.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnUpdateWorkout.setOnClickListener(v -> updateWorkout());
        btnDeleteWorkout.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void updateWorkout() {
        // Clear previous errors
        tilWorkoutType.setError(null);
        tilWorkoutDate.setError(null);
        tilDuration.setError(null);
        tilCalories.setError(null);

        String workoutType = etWorkoutType.getText().toString().trim();
        String workoutDate = etWorkoutDate.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String calories = etCalories.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        boolean hasError = false;

        // Validate workout type
        if (TextUtils.isEmpty(workoutType)) {
            tilWorkoutType.setError("Please select a workout type");
            hasError = true;
        }

        // Validate date
        if (TextUtils.isEmpty(workoutDate)) {
            tilWorkoutDate.setError("Please select a workout date");
            hasError = true;
        }

        // Validate duration
        if (TextUtils.isEmpty(duration)) {
            tilDuration.setError("Duration is required");
            hasError = true;
        } else {
            try {
                int durationValue = Integer.parseInt(duration);
                if (durationValue <= 0) {
                    tilDuration.setError("Duration must be greater than 0");
                    hasError = true;
                } else if (durationValue > 1440) { // 24 hours
                    tilDuration.setError("Duration cannot exceed 24 hours");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilDuration.setError("Duration must be a valid number");
                hasError = true;
            }
        }

        // Validate calories
        if (TextUtils.isEmpty(calories)) {
            tilCalories.setError("Calories burned is required");
            hasError = true;
        } else {
            try {
                int caloriesValue = Integer.parseInt(calories);
                if (caloriesValue < 0) {
                    tilCalories.setError("Calories cannot be negative");
                    hasError = true;
                } else if (caloriesValue > 10000) { // Reasonable upper limit
                    tilCalories.setError("Calories value seems too high");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilCalories.setError("Calories must be a valid number");
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        // Update in database
        String workoutDateTime = workoutDate + " " + getCurrentTime();
        int durationValue = Integer.parseInt(duration);
        int caloriesValue = Integer.parseInt(calories);

        if (databaseHelper.updateWorkout(workoutId, workoutType, durationValue, caloriesValue, notes.isEmpty() ? "" : notes, workoutDateTime)) {
            Toast.makeText(this, "Workout updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to update workout", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Workout?")
                .setMessage("Are you sure you want to delete this " + currentWorkout.getWorkoutType() + " workout? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteWorkout();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteWorkout() {
        if (databaseHelper.deleteWorkout(workoutId)) {
            Toast.makeText(this, "Workout deleted successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete workout", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return timeFormat.format(System.currentTimeMillis());
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

