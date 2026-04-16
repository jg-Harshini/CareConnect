package com.example.careconnect.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class ContactRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addContact(userId: Int, name: String, phone: String, relation: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CONTACT_USER_ID, userId)
            put(DatabaseHelper.COLUMN_CONTACT_NAME, name)
            put(DatabaseHelper.COLUMN_CONTACT_PHONE, phone)
            put(DatabaseHelper.COLUMN_CONTACT_RELATION, relation)
        }
        return db.insert(DatabaseHelper.TABLE_CONTACTS, null, values)
    }

    fun getPrimaryContactForUser(userId: Int): EmergencyContact? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_CONTACTS,
            null,
            "${DatabaseHelper.COLUMN_CONTACT_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null, "1"
        )

        return if (cursor.moveToFirst()) {
            val contact = EmergencyContact(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_NAME)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_PHONE)),
                relation = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT_RELATION))
            )
            cursor.close()
            contact
        } else {
            cursor.close()
            null
        }
    }
}

data class EmergencyContact(
    val id: Int,
    val userId: Int,
    val name: String,
    val phone: String,
    val relation: String
)
