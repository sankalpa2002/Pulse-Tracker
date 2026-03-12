package com.example.pulsetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkoutHistoryActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutClickListener {

    private RecyclerView workoutRecyclerView;
    private LinearLayout emptyStateLayout;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> workoutList;
    private List<Workout> filteredWorkoutList;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private AutoCompleteTextView filterSpinner;
    private MaterialButton btnClearFilter;
    private MaterialButton btnBack;
    private MaterialButton btnThemeToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        applyTheme();

        EdgeToEdge.enable(this);
        setContentView(R.layout.workout_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        loadWorkouts();
        setupFilterSpinner();
        setupListeners();
    }

    private void initViews() {
        workoutRecyclerView = findViewById(R.id.workoutRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        filterSpinner = findViewById(R.id.filterSpinner);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        btnBack = findViewById(R.id.btnBack);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);

        updateThemeIcon(btnThemeToggle);
    }

    private void setupRecyclerView() {
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutList = new ArrayList<>();
        filteredWorkoutList = new ArrayList<>();
        workoutAdapter = new WorkoutAdapter(this, workoutList, this);
        workoutRecyclerView.setAdapter(workoutAdapter);
    }

    private void loadWorkouts() {
        int userId = sessionManager.getUserId();
        workoutList = databaseHelper.getWorkoutsByUser(userId);
        filteredWorkoutList = new ArrayList<>(workoutList);
        workoutAdapter.updateData(filteredWorkoutList);
        updateEmptyState();
    }

    private void setupFilterSpinner() {
        // Extract unique workout types from the list
        Set<String> workoutTypes = new HashSet<>();
        workoutTypes.add("All Types"); // Add "All Types" option
        for (Workout workout : workoutList) {
            workoutTypes.add(workout.getWorkoutType());
        }

        List<String> typesList = new ArrayList<>(workoutTypes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                typesList
        );
        filterSpinner.setAdapter(adapter);
        filterSpinner.setText("All Types", false);

        // Filter when selection changes
        filterSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = (String) parent.getItemAtPosition(position);
            filterByWorkoutType(selectedType);
        });
    }

    private void filterByWorkoutType(String type) {
        if (type.equals("All Types")) {
            filteredWorkoutList = new ArrayList<>(workoutList);
        } else {
            filteredWorkoutList = new ArrayList<>();
            for (Workout workout : workoutList) {
                if (workout.getWorkoutType().equalsIgnoreCase(type)) {
                    filteredWorkoutList.add(workout);
                }
            }
        }
        workoutAdapter.updateData(filteredWorkoutList);
        updateEmptyState();
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

        btnClearFilter.setOnClickListener(v -> {
            filterSpinner.setText("All Types", false);
            filteredWorkoutList = new ArrayList<>(workoutList);
            workoutAdapter.updateData(filteredWorkoutList);
            updateEmptyState();
        });

        // Add Workout button from empty state
        MaterialButton btnAddWorkoutEmpty = emptyStateLayout.findViewById(R.id.btnAddWorkoutEmpty);
        if (btnAddWorkoutEmpty != null) {
            btnAddWorkoutEmpty.setOnClickListener(v -> {
                Intent intent = new Intent(WorkoutHistoryActivity.this, AddWorkoutActivity.class);
                startActivityForResult(intent, 1);
            });
        }
    }

    private void updateEmptyState() {
        if (filteredWorkoutList.isEmpty()) {
            emptyStateLayout.setVisibility(androidx.constraintlayout.widget.ConstraintLayout.VISIBLE);
            workoutRecyclerView.setVisibility(androidx.constraintlayout.widget.ConstraintLayout.GONE);
        } else {
            emptyStateLayout.setVisibility(androidx.constraintlayout.widget.ConstraintLayout.GONE);
            workoutRecyclerView.setVisibility(androidx.constraintlayout.widget.ConstraintLayout.VISIBLE);
        }
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

    @Override
    public void onEditClick(Workout workout) {
        Intent intent = new Intent(WorkoutHistoryActivity.this, UpdateWorkoutActivity.class);
        intent.putExtra(UpdateWorkoutActivity.WORKOUT_ID_EXTRA, workout.getId());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onDeleteClick(Workout workout) {
        showDeleteConfirmationDialog(workout);
    }

    private void showDeleteConfirmationDialog(Workout workout) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Workout?")
                .setMessage("Are you sure you want to delete this " + workout.getWorkoutType() + " workout?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteWorkout(workout);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteWorkout(Workout workout) {
        if (databaseHelper.deleteWorkout(workout.getId())) {
            // Remove from filtered list
            filteredWorkoutList.remove(workout);
            // Remove from main list
            workoutList.remove(workout);
            // Update adapter
            workoutAdapter.updateData(filteredWorkoutList);
            updateEmptyState();
            Toast.makeText(this, "Workout deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete workout", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Refresh workouts list
            loadWorkouts();
        }
    }
}

