package com.example.careconnect.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class ReminderRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addReminder(userId: Int, title: String, subtitle: String, time: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_REMINDER_USER_ID, userId)
            put(DatabaseHelper.COLUMN_REMINDER_TITLE, title)
            put(DatabaseHelper.COLUMN_REMINDER_SUBTITLE, subtitle)
            put(DatabaseHelper.COLUMN_REMINDER_TIME, time)
            put(DatabaseHelper.COLUMN_REMINDER_STATUS, "PENDING")
        }
        db.insert(DatabaseHelper.TABLE_REMINDERS, null, values)
    }

    fun getRemindersForUser(userId: Int): List<Reminder> {
        val reminders = mutableListOf<Reminder>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_REMINDERS,
            null,
            "${DatabaseHelper.COLUMN_REMINDER_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, "${DatabaseHelper.COLUMN_REMINDER_TIME} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                reminders.add(
                    Reminder(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_USER_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_TITLE)),
                        subtitle = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_SUBTITLE)),
                        time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_TIME)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_STATUS)),
                        lastTriggerDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_LAST_TRIGGER))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return reminders
    }

    fun updateReminderStatus(reminderId: Int, status: String, date: String? = null) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_REMINDER_STATUS, status)
            if (date != null) {
                put(DatabaseHelper.COLUMN_REMINDER_LAST_TRIGGER, date)
            }
        }
        db.update(
            DatabaseHelper.TABLE_REMINDERS,
            values,
            "${DatabaseHelper.COLUMN_REMINDER_ID} = ?",
            arrayOf(reminderId.toString())
        )
    }
    
    fun deleteReminder(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_REMINDERS, "${DatabaseHelper.COLUMN_REMINDER_ID} = ?", arrayOf(id.toString()))
    }
}

data class Reminder(
    val id: Int,
    val userId: Int,
    val title: String,
    val subtitle: String,
    val time: String,
    val status: String,
    val lastTriggerDate: String?
)
