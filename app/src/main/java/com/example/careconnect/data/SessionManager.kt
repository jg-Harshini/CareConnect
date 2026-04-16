package com.example.careconnect.data

import android.content.ContentValues
import android.content.Context

class SessionManager(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun saveUserSession(userId: Int, name: String, role: String) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_SESSION, null, null)
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SESSION_USER_ID, userId)
            put(DatabaseHelper.COLUMN_SESSION_ACTIVE, 1)
        }
        db.insert(DatabaseHelper.TABLE_SESSION, null, values)
    }

    fun isLoggedIn(): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SESSION,
            null,
            "${DatabaseHelper.COLUMN_SESSION_ACTIVE} = 1",
            null, null, null, null
        )
        val loggedIn = cursor.count > 0
        cursor.close()
        return loggedIn
    }

    fun getUserId(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SESSION,
            arrayOf(DatabaseHelper.COLUMN_SESSION_USER_ID),
            "${DatabaseHelper.COLUMN_SESSION_ACTIVE} = 1",
            null, null, null, null
        )
        var userId = -1
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SESSION_USER_ID))
        }
        cursor.close()
        return userId
    }

    fun getUserName(): String {
        val userId = getUserId()
        if (userId == -1) return "User"
        
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_NAME),
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        var name = "User"
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME))
        }
        cursor.close()
        return name
    }

    fun getUserRole(): String {
        val userId = getUserId()
        if (userId == -1) return ""
        
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_ROLE),
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        var role = ""
        if (cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE))
        }
        cursor.close()
        return role
    }

    fun logout() {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_SESSION, null, null)
    }
}
