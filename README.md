# Pulse Tracker

A fitness workout tracking Android application built with Java and Material Design 3.

## Features

- **User Authentication** — Register and login with email/password (SHA-256 hashed)
- **Dashboard** — View total workouts, calories burned, and duration at a glance
- **Add Workout** — Log workouts with type, date, duration, calories, and notes
- **Workout History** — Browse, filter by type, edit, and delete past workouts
- **Dark/Light Mode** — Toggle theme across all screens with a persistent preference
- **Session Management** — Stay logged in across app restarts

## Screenshots

| Light Mode | Dark Mode |
|:---:|:---:|
| Dashboard | Dashboard |

## Tech Stack

- **Language:** Java
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36
- **UI:** Material Design 3 (Material Components)
- **Database:** SQLite (via SQLiteOpenHelper)
- **Session:** SharedPreferences
- **Build:** Gradle with version catalogs

## Project Structure

```
app/src/main/
├── java/com/example/pulsetracker/
│   ├── MainActivity.java          # Dashboard with stats & navigation
│   ├── LoginActivity.java         # User login
│   ├── RegisterActivity.java      # User registration
│   ├── AddWorkoutActivity.java    # Add new workout
│   ├── UpdateWorkoutActivity.java # Edit/delete existing workout
│   ├── WorkoutHistoryActivity.java# Workout list with filtering
│   ├── DatabaseHelper.java        # SQLite database operations
│   ├── SessionManager.java        # SharedPreferences session handling
│   ├── WorkoutAdapter.java        # RecyclerView adapter
│   ├── User.java                  # User model
│   └── Workout.java               # Workout model
├── res/
│   ├── layout/                    # XML layouts for all screens
│   ├── drawable/                  # Vector icons and backgrounds
│   ├── values/                    # Colors, strings, light theme
│   └── values-night/              # Dark theme overrides
└── AndroidManifest.xml
```

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17+

### Build & Run

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator or device (API 24+)

## Database Schema

### Users
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key, auto-increment |
| full_name | TEXT | User's display name |
| email | TEXT | Unique email address |
| password | TEXT | SHA-256 hashed password |
| created_at | DATETIME | Account creation timestamp |

### Workouts
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key, auto-increment |
| user_id | INTEGER | Foreign key → Users |
| workout_type | TEXT | Running, Cycling, Swimming, etc. |
| duration | INTEGER | Duration in minutes |
| calories | INTEGER | Calories burned |
| notes | TEXT | Optional notes |
| workout_date | DATETIME | Date of workout |
| created_at | DATETIME | Record creation timestamp |

## License

This project is for educational purposes.
