package com.example.pulsetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PulseTracker.db";
    private static final int DATABASE_VERSION = 2;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_FULL_NAME = "full_name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_CREATED_AT = "created_at";

    // Workouts table
    private static final String TABLE_WORKOUTS = "workouts";
    private static final String COL_WORKOUT_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_WORKOUT_TYPE = "workout_type";
    private static final String COL_DURATION = "duration";
    private static final String COL_CALORIES = "calories";
    private static final String COL_NOTES = "notes";
    private static final String COL_WORKOUT_DATE = "workout_date";
    private static final String COL_WORKOUT_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FULL_NAME + " TEXT NOT NULL, "
                + COL_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createUsersTable);

        String createWorkoutsTable = "CREATE TABLE " + TABLE_WORKOUTS + " ("
                + COL_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_ID + " INTEGER NOT NULL, "
                + COL_WORKOUT_TYPE + " TEXT NOT NULL, "
                + COL_DURATION + " INTEGER NOT NULL, "
                + COL_CALORIES + " INTEGER NOT NULL, "
                + COL_NOTES + " TEXT, "
                + COL_WORKOUT_DATE + " DATETIME NOT NULL, "
                + COL_WORKOUT_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + "))";
        db.execSQL(createWorkoutsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String createWorkoutsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUTS + " ("
                    + COL_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_USER_ID + " INTEGER NOT NULL, "
                    + COL_WORKOUT_TYPE + " TEXT NOT NULL, "
                    + COL_DURATION + " INTEGER NOT NULL, "
                    + COL_CALORIES + " INTEGER NOT NULL, "
                    + COL_NOTES + " TEXT, "
                    + COL_WORKOUT_DATE + " DATETIME NOT NULL, "
                    + COL_WORKOUT_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + "))";
            db.execSQL(createWorkoutsTable);
        }
    }

    /**
     * Hashes a password using SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    /**
     * Registers a new user. Returns true on success.
     */
    public boolean registerUser(String fullName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_EMAIL, email.toLowerCase().trim());
        values.put(COL_PASSWORD, hashPassword(password));

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_USERS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result != -1;
    }

    /**
     * Authenticates a user with email and password.
     */
    public boolean authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email.toLowerCase().trim(), hashedPassword});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Checks whether an email is already registered.
     */
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Retrieves a User object by email address.
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()});

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        }
        cursor.close();
        return user;
    }

    // ==================== WORKOUT METHODS ====================

    /**
     * Inserts a new workout into the database.
     */
    public boolean addWorkout(int userId, String workoutType, int duration, int calories, String notes, String workoutDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_WORKOUT_TYPE, workoutType);
        values.put(COL_DURATION, duration);
        values.put(COL_CALORIES, calories);
        values.put(COL_NOTES, notes);
        values.put(COL_WORKOUT_DATE, workoutDate);

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_WORKOUTS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result != -1;
    }

    /**
     * Retrieves all workouts for a specific user, sorted by date descending.
     */
    public java.util.List<Workout> getWorkoutsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<Workout> workouts = new java.util.ArrayList<>();

        String query = "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COL_USER_ID + " = ? ORDER BY " + COL_WORKOUT_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Workout workout = new Workout();
                workout.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_WORKOUT_ID)));
                workout.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
                workout.setWorkoutType(cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_TYPE)));
                workout.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)));
                workout.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALORIES)));
                workout.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
                workout.setWorkoutDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_DATE)));
                workout.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_CREATED_AT)));
                workouts.add(workout);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return workouts;
    }

    /**
     * Retrieves a single workout by ID.
     */
    public Workout getWorkoutById(int workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COL_WORKOUT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(workoutId)});

        Workout workout = null;
        if (cursor.moveToFirst()) {
            workout = new Workout();
            workout.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_WORKOUT_ID)));
            workout.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            workout.setWorkoutType(cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_TYPE)));
            workout.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)));
            workout.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALORIES)));
            workout.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
            workout.setWorkoutDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_DATE)));
            workout.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_CREATED_AT)));
        }
        cursor.close();
        return workout;
    }

    /**
     * Updates an existing workout.
     */
    public boolean updateWorkout(int workoutId, String workoutType, int duration, int calories, String notes, String workoutDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_TYPE, workoutType);
        values.put(COL_DURATION, duration);
        values.put(COL_CALORIES, calories);
        values.put(COL_NOTES, notes);
        values.put(COL_WORKOUT_DATE, workoutDate);

        int rowsUpdated = db.update(TABLE_WORKOUTS, values, COL_WORKOUT_ID + " = ?", new String[]{String.valueOf(workoutId)});
        return rowsUpdated > 0;
    }

    /**
     * Deletes a workout by ID.
     */
    public boolean deleteWorkout(int workoutId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_WORKOUTS, COL_WORKOUT_ID + " = ?", new String[]{String.valueOf(workoutId)});
        return rowsDeleted > 0;
    }
}
