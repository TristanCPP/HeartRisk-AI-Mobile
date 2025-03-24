package com.example.ai_mobileapp_test

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

// DatabaseHelper class manages the database operations for user registration and login
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    data class RiskAssessment(
        val date: String,  // Added date field
        val riskScore: Double,
        val chestPainType: String,
        val restingBP: Int,
        val cholesterol: Int,
        val maxHeartRate: Int,
        val exerciseAngina: String,
        val oldpeak: Double,
        val stSlope: String
    )
    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_SEX = "sex"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
        CREATE TABLE users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT UNIQUE,
            password TEXT,
            age INTEGER,
            sex TEXT
        )
    """.trimIndent()

        val createAssessmentsTable = """
        CREATE TABLE assessments (
            assessment_id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            chest_pain_type TEXT,
            resting_bp INTEGER,
            cholesterol INTEGER,
            max_hr INTEGER,
            exercise_angina TEXT,
            oldpeak REAL,
            st_slope TEXT,
            risk_score REAL,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        )
    """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createAssessmentsTable)
        Log.i("DatabaseHelper", "Database created successfully")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            val createAssessmentsTable = """
            CREATE TABLE IF NOT EXISTS assessments (
                assessment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                chest_pain_type TEXT,
                resting_bp INTEGER,
                cholesterol INTEGER,
                max_hr INTEGER,
                exercise_angina TEXT,
                oldpeak REAL,
                st_slope TEXT,
                risk_score REAL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """.trimIndent()
            db.execSQL(createAssessmentsTable)
            Log.i("DatabaseHelper", "Database upgraded to include assessments table")
        }
    }
    fun registerUser(email: String, password: String, age: String, sex: String): Boolean {
        val db = writableDatabase
        val normalizedEmail = email.trim().lowercase()
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE LOWER(TRIM($COLUMN_EMAIL)) = ?", arrayOf(normalizedEmail))
        val userExists = cursor.count > 0
        cursor.close()

        if (userExists) {
            db.close()
            Log.e("DatabaseHelper", "User already exists: $normalizedEmail")
            return false
        }

        val values = ContentValues().apply {
            put(COLUMN_EMAIL, normalizedEmail)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_AGE, age.toIntOrNull() ?: 0)
            put(COLUMN_SEX, sex.trim().uppercase())
        }

        return try {
            val result = db.insert(TABLE_USERS, null, values)
            db.close()
            if (result == -1L) {
                Log.e("DatabaseHelper", "Failed to insert user: $normalizedEmail")
            } else {
                Log.i("DatabaseHelper", "User registered successfully: $normalizedEmail")
            }
            result != -1L
        } catch (e: Exception) {
            db.close()
            Log.e("DatabaseHelper", "Error inserting user: ${e.message}")
            false
        }
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getUserID(email: String, password: String): Int? {
        val db = this.readableDatabase
        val query = "SELECT id FROM users WHERE email = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))

        return if (cursor.moveToFirst()) {
            val userID = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            cursor.close()
            db.close()
            userID
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun getUserDetails(userID: Int): Pair<Int, String>? {
        val db = this.readableDatabase
        val query = "SELECT age, sex FROM users WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(userID.toString()))

        return if (cursor.moveToFirst()) {
            val age = cursor.getInt(cursor.getColumnIndexOrThrow("age"))
            val sex = cursor.getString(cursor.getColumnIndexOrThrow("sex"))
            cursor.close()
            db.close()
            Pair(age, sex)
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun insertRiskAssessment(
        userID: Int,
        chestPainType: String,
        restingBP: Int,
        cholesterol: Int,
        maxHR: Int,
        exerciseAngina: String,
        oldpeak: Double,
        stSlope: String,
        riskScore: Double
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userID)
            put("chest_pain_type", chestPainType)
            put("resting_bp", restingBP)
            put("cholesterol", cholesterol)
            put("max_hr", maxHR)
            put("exercise_angina", exerciseAngina)
            put("oldpeak", oldpeak)
            put("st_slope", stSlope)
            put("risk_score", riskScore)
        }

        val result = db.insert("assessments", null, values)
        db.close()
        return result != -1L
    }

    fun getUserAssessments(userID: Int): List<RiskAssessment> {
        val assessments = mutableListOf<RiskAssessment>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT timestamp, risk_score, chest_pain_type, resting_bp, cholesterol, max_hr, exercise_angina, oldpeak, st_slope FROM assessments WHERE user_id = ? ORDER BY timestamp ASC", arrayOf(userID.toString()))

        while (cursor.moveToNext()) {
            val riskAssessment = RiskAssessment(
                date = cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                riskScore = cursor.getDouble(cursor.getColumnIndexOrThrow("risk_score")),
                chestPainType = cursor.getString(cursor.getColumnIndexOrThrow("chest_pain_type")),
                restingBP = cursor.getInt(cursor.getColumnIndexOrThrow("resting_bp")),
                cholesterol = cursor.getInt(cursor.getColumnIndexOrThrow("cholesterol")),
                maxHeartRate = cursor.getInt(cursor.getColumnIndexOrThrow("max_hr")),
                exerciseAngina = cursor.getString(cursor.getColumnIndexOrThrow("exercise_angina")),
                oldpeak = cursor.getDouble(cursor.getColumnIndexOrThrow("oldpeak")),
                stSlope = cursor.getString(cursor.getColumnIndexOrThrow("st_slope"))
            )
            assessments.add(riskAssessment)
        }
        cursor.close()
        db.close()
        return assessments
    }
}