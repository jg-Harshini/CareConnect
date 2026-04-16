package com.example.careconnect.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import java.util.UUID

class UserRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun registerUser(name: String, email: String, password: String, role: String): Long {
        val db = dbHelper.writableDatabase
        val patientId = if (role == "patient") generateUniquePatientId() else null
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, name)
            put(DatabaseHelper.COLUMN_EMAIL, email)
            put(DatabaseHelper.COLUMN_PASSWORD, password)
            put(DatabaseHelper.COLUMN_ROLE, role)
            put(DatabaseHelper.COLUMN_PATIENT_ID, patientId)
        }
        return db.insert(DatabaseHelper.TABLE_USERS, null, values)
    }

    private fun generateUniquePatientId(): String {
        return UUID.randomUUID().toString().substring(0, 8).uppercase()
    }

    fun loginUser(email: String, password: String): User? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_EMAIL} = ? AND ${DatabaseHelper.COLUMN_PASSWORD} = ?",
            arrayOf(email, password),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = mapCursorToUser(cursor)
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun linkPatientToCaretaker(patientUniqueId: String, caretakerId: Int): Boolean {
        val db = dbHelper.writableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_ID),
            "${DatabaseHelper.COLUMN_PATIENT_ID} = ? AND ${DatabaseHelper.COLUMN_ROLE} = ?",
            arrayOf(patientUniqueId, "patient"),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val patientId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
            cursor.close()

            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_LINKED_USER_ID, caretakerId)
            }
            val rows = db.update(DatabaseHelper.TABLE_USERS, values, "${DatabaseHelper.COLUMN_ID} = ?", arrayOf(patientId.toString()))
            return rows > 0
        }
        cursor.close()
        return false
    }

    fun getUserById(userId: Int): User? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = mapCursorToUser(cursor)
            cursor.close()
            
            if (user.role == "patient" && user.patientUniqueId == null) {
                val newId = generateUniquePatientId()
                val values = ContentValues().apply { put(DatabaseHelper.COLUMN_PATIENT_ID, newId) }
                dbHelper.writableDatabase.update(DatabaseHelper.TABLE_USERS, values, "${DatabaseHelper.COLUMN_ID} = ?", arrayOf(userId.toString()))
                return user.copy(patientUniqueId = newId)
            }
            user
        } else {
            cursor.close()
            null
        }
    }

    fun getLinkedUser(userId: Int): User? {
        val user = getUserById(userId) ?: return null
        val linkedId = user.linkedUserId ?: return null
        return getUserById(linkedId)
    }

    fun getPatientsForCaretaker(caretakerId: Int): List<User> {
        val list = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_LINKED_USER_ID} = ? AND ${DatabaseHelper.COLUMN_ROLE} = ?",
            arrayOf(caretakerId.toString(), "patient"),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(mapCursorToUser(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun setHomeLocation(userId: Int, lat: Double, lng: Double) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_HOME_LAT, lat)
            put(DatabaseHelper.COLUMN_HOME_LNG, lng)
        }
        db.update(DatabaseHelper.TABLE_USERS, values, "${DatabaseHelper.COLUMN_ID} = ?", arrayOf(userId.toString()))
    }

    private fun mapCursorToUser(cursor: Cursor): User {
        val linkedIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LINKED_USER_ID)
        val pIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATIENT_ID)
        val latIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HOME_LAT)
        val lngIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HOME_LNG)
        
        return User(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
            role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE)),
            patientUniqueId = if (cursor.isNull(pIdIndex)) null else cursor.getString(pIdIndex),
            linkedUserId = if (cursor.isNull(linkedIdIndex)) null else cursor.getInt(linkedIdIndex),
            homeLat = if (cursor.isNull(latIndex)) 0.0 else cursor.getDouble(latIndex),
            homeLng = if (cursor.isNull(lngIndex)) 0.0 else cursor.getDouble(lngIndex)
        )
    }
}

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val patientUniqueId: String? = null,
    val linkedUserId: Int? = null,
    val homeLat: Double = 0.0,
    val homeLng: Double = 0.0
)
