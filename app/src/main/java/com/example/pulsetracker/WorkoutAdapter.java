package com.example.pulsetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts;
    private Context context;
    private OnWorkoutClickListener onWorkoutClickListener;

    public interface OnWorkoutClickListener {
        void onEditClick(Workout workout);
        void onDeleteClick(Workout workout);
    }

    public WorkoutAdapter(Context context, List<Workout> workouts, OnWorkoutClickListener listener) {
        this.context = context;
        this.workouts = new ArrayList<>(workouts);
        this.onWorkoutClickListener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    /**
     * Updates the adapter data with a new list of workouts.
     */
    public void updateData(List<Workout> newWorkouts) {
        this.workouts = new ArrayList<>(newWorkouts);
        notifyDataSetChanged();
    }

    /**
     * Filters workouts by type.
     */
    public void filterByType(String type) {
        List<Workout> filteredList = new ArrayList<>();
        for (Workout workout : workouts) {
            if (workout.getWorkoutType().toLowerCase().contains(type.toLowerCase())) {
                filteredList.add(workout);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Removes a workout from the list.
     */
    public void removeWorkout(int position) {
        if (position >= 0 && position < workouts.size()) {
            workouts.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Returns current list of workouts.
     */
    public List<Workout> getWorkouts() {
        return new ArrayList<>(workouts);
    }

    /**
     * Inner ViewHolder class to bind workout data to views.
     */
    public class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private TextView tvWorkoutType;
        private TextView tvWorkoutDate;
        private TextView tvDuration;
        private TextView tvCalories;
        private MaterialButton btnEditWorkout;
        private Workout currentWorkout;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutType = itemView.findViewById(R.id.tvWorkoutType);
            tvWorkoutDate = itemView.findViewById(R.id.tvWorkoutDate);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            btnEditWorkout = itemView.findViewById(R.id.btnEditWorkout);
        }

        public void bind(Workout workout) {
            this.currentWorkout = workout;

            tvWorkoutType.setText(workout.getWorkoutType());
            tvWorkoutDate.setText(formatDate(workout.getWorkoutDate()));
            tvDuration.setText(workout.getDuration() + " min");
            tvCalories.setText(workout.getCalories() + " kcal");

            // Edit button click listener
            btnEditWorkout.setOnClickListener(v -> {
                if (onWorkoutClickListener != null) {
                    onWorkoutClickListener.onEditClick(currentWorkout);
                }
            });

            // Long click for delete
            itemView.setOnLongClickListener(v -> {
                if (onWorkoutClickListener != null) {
                    onWorkoutClickListener.onDeleteClick(currentWorkout);
                }
                return true;
            });
        }

        /**
         * Formats the workout date for display.
         */
        private String formatDate(String dateTime) {
            // Input format: YYYY-MM-DD HH:MM:SS
            // Output format: MMM DD, YYYY
            if (dateTime == null || dateTime.isEmpty()) return "";
            try {
                String[] parts = dateTime.split(" ");
                return parts[0]; // Return just the date part
            } catch (Exception e) {
                return dateTime;
            }
        }
    }
}

