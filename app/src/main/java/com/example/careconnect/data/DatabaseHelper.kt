package com.example.careconnect.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CareConnect.db"
        private const val DATABASE_VERSION = 7 // Incremented version

        // User table
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_ROLE = "role"
        const val COLUMN_PATIENT_ID = "patient_unique_id"
        const val COLUMN_LINKED_USER_ID = "linked_user_id" 
        const val COLUMN_HOME_LAT = "home_lat"
        const val COLUMN_HOME_LNG = "home_lng"

        // Session table
        const val TABLE_SESSION = "active_session"
        const val COLUMN_SESSION_USER_ID = "user_id"
        const val COLUMN_SESSION_ACTIVE = "is_active"

        // Reminders table
        const val TABLE_REMINDERS = "reminders"
        const val COLUMN_REMINDER_ID = "id"
        const val COLUMN_REMINDER_USER_ID = "user_id"
        const val COLUMN_REMINDER_TITLE = "title"
        const val COLUMN_REMINDER_SUBTITLE = "subtitle"
        const val COLUMN_REMINDER_TIME = "time"
        const val COLUMN_REMINDER_STATUS = "status"
        const val COLUMN_REMINDER_LAST_TRIGGER = "last_trigger_date"

        // Location History
        const val TABLE_LOCATION_HISTORY = "location_history"
        const val COLUMN_LOC_ID = "id"
        const val COLUMN_LOC_USER_ID = "user_id"
        const val COLUMN_LOC_LAT = "latitude"
        const val COLUMN_LOC_LNG = "longitude"
        const val COLUMN_LOC_TIMESTAMP = "timestamp"
        const val COLUMN_LOC_ADDRESS = "address"
        const val COLUMN_LOC_IS_BREACH = "is_breach"

        // Safe Zones
        const val TABLE_SAFE_ZONES = "safe_zones"
        const val COLUMN_ZONE_ID = "id"
        const val COLUMN_ZONE_USER_ID = "user_id"
        const val COLUMN_ZONE_NAME = "name"
        const val COLUMN_ZONE_LAT = "center_lat"
        const val COLUMN_ZONE_LNG = "center_lng"
        const val COLUMN_ZONE_RADIUS = "radius_meters"

        // FOG Incidents
        const val TABLE_FOG = "fog_incidents"
        const val COLUMN_FOG_ID = "id"
        const val COLUMN_FOG_USER_ID = "user_id"
        const val COLUMN_FOG_TIMESTAMP = "timestamp"
        const val COLUMN_FOG_DURATION = "duration_seconds"
        const val COLUMN_FOG_LAT = "latitude"
        const val COLUMN_FOG_LNG = "longitude"

        // Fall Incidents
        const val TABLE_FALLS = "fall_incidents"
        const val COLUMN_FALL_ID = "id"
        const val COLUMN_FALL_USER_ID = "user_id"
        const val COLUMN_FALL_TIMESTAMP = "timestamp"
        const val COLUMN_FALL_TYPE = "type"
        const val COLUMN_FALL_LAT = "latitude"
        const val COLUMN_FALL_LNG = "longitude"

        // Emergency Contacts table
        const val TABLE_CONTACTS = "emergency_contacts"
        const val COLUMN_CONTACT_ID = "id"
        const val COLUMN_CONTACT_USER_ID = "user_id"
        const val COLUMN_CONTACT_NAME = "name"
        const val COLUMN_CONTACT_PHONE = "phone"
        const val COLUMN_CONTACT_RELATION = "relation"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_NAME TEXT, $COLUMN_EMAIL TEXT UNIQUE, $COLUMN_PASSWORD TEXT, $COLUMN_ROLE TEXT, $COLUMN_PATIENT_ID TEXT, $COLUMN_LINKED_USER_ID INTEGER, $COLUMN_HOME_LAT REAL, $COLUMN_HOME_LNG REAL)")
        
        db.execSQL("CREATE TABLE $TABLE_SESSION ($COLUMN_SESSION_USER_ID INTEGER PRIMARY KEY, $COLUMN_SESSION_ACTIVE INTEGER DEFAULT 0, FOREIGN KEY($COLUMN_SESSION_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID))")

        db.execSQL("CREATE TABLE $TABLE_REMINDERS ($COLUMN_REMINDER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_REMINDER_USER_ID INTEGER, $COLUMN_REMINDER_TITLE TEXT, $COLUMN_REMINDER_SUBTITLE TEXT, $COLUMN_REMINDER_TIME TEXT, $COLUMN_REMINDER_STATUS TEXT DEFAULT 'PENDING', $COLUMN_REMINDER_LAST_TRIGGER TEXT)")

        db.execSQL("CREATE TABLE $TABLE_LOCATION_HISTORY ($COLUMN_LOC_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_LOC_USER_ID INTEGER, $COLUMN_LOC_LAT REAL, $COLUMN_LOC_LNG REAL, $COLUMN_LOC_TIMESTAMP INTEGER, $COLUMN_LOC_ADDRESS TEXT, $COLUMN_LOC_IS_BREACH INTEGER DEFAULT 0)")

        db.execSQL("CREATE TABLE $TABLE_SAFE_ZONES ($COLUMN_ZONE_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_ZONE_USER_ID INTEGER, $COLUMN_ZONE_NAME TEXT, $COLUMN_ZONE_LAT REAL, $COLUMN_ZONE_LNG REAL, $COLUMN_ZONE_RADIUS REAL)")

        db.execSQL("CREATE TABLE $TABLE_FOG ($COLUMN_FOG_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_FOG_USER_ID INTEGER, $COLUMN_FOG_TIMESTAMP INTEGER, $COLUMN_FOG_DURATION INTEGER, $COLUMN_FOG_LAT REAL, $COLUMN_FOG_LNG REAL)")

        db.execSQL("CREATE TABLE $TABLE_FALLS ($COLUMN_FALL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_FALL_USER_ID INTEGER, $COLUMN_FALL_TIMESTAMP INTEGER, $COLUMN_FALL_TYPE TEXT, $COLUMN_FALL_LAT REAL, $COLUMN_FALL_LNG REAL)")

        db.execSQL("CREATE TABLE $TABLE_CONTACTS ($COLUMN_CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_CONTACT_USER_ID INTEGER, $COLUMN_CONTACT_NAME TEXT, $COLUMN_CONTACT_PHONE TEXT, $COLUMN_CONTACT_RELATION TEXT, FOREIGN KEY($COLUMN_CONTACT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATION_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAFE_ZONES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FALLS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }
}
