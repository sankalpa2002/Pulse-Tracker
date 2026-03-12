package com.example.pulsetracker;

public class Workout {
    private int id;
    private int userId;
    private String workoutType;
    private int duration; // in minutes
    private int calories;
    private String notes;
    private String workoutDate; // Format: YYYY-MM-DD HH:MM:SS
    private String createdAt;

    public Workout() {}

    public Workout(int id, int userId, String workoutType, int duration, int calories, String notes, String workoutDate, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.workoutType = workoutType;
        this.duration = duration;
        this.calories = calories;
        this.notes = notes;
        this.workoutDate = workoutDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getWorkoutDate() { return workoutDate; }
    public void setWorkoutDate(String workoutDate) { this.workoutDate = workoutDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", userId=" + userId +
                ", workoutType='" + workoutType + '\'' +
                ", duration=" + duration +
                ", calories=" + calories +
                ", notes='" + notes + '\'' +
                ", workoutDate='" + workoutDate + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}

