package com.example.careconnect.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class LocationRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // --- Location History ---

    fun logLocation(userId: Int, latitude: Double, longitude: Double, address: String, isBreach: Boolean) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_LOC_USER_ID, userId)
            put(DatabaseHelper.COLUMN_LOC_LAT, latitude)
            put(DatabaseHelper.COLUMN_LOC_LNG, longitude)
            put(DatabaseHelper.COLUMN_LOC_TIMESTAMP, System.currentTimeMillis())
            put(DatabaseHelper.COLUMN_LOC_ADDRESS, address)
            put(DatabaseHelper.COLUMN_LOC_IS_BREACH, if (isBreach) 1 else 0)
        }
        db.insert(DatabaseHelper.TABLE_LOCATION_HISTORY, null, values)
    }

    fun getHistoryForUser(userId: Int, limit: Int = 50): List<LocationPoint> {
        val list = mutableListOf<LocationPoint>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_LOCATION_HISTORY,
            null,
            "${DatabaseHelper.COLUMN_LOC_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, "${DatabaseHelper.COLUMN_LOC_TIMESTAMP} DESC", limit.toString()
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    LocationPoint(
                        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOC_LAT)),
                        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOC_LNG)),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOC_TIMESTAMP)),
                        address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOC_ADDRESS)),
                        isBreach = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOC_IS_BREACH)) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- Safe Zones ---

    fun saveSafeZone(userId: Int, name: String, lat: Double, lng: Double, radius: Float) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ZONE_USER_ID, userId)
            put(DatabaseHelper.COLUMN_ZONE_NAME, name)
            put(DatabaseHelper.COLUMN_ZONE_LAT, lat)
            put(DatabaseHelper.COLUMN_ZONE_LNG, lng)
            put(DatabaseHelper.COLUMN_ZONE_RADIUS, radius)
        }
        db.insert(DatabaseHelper.TABLE_SAFE_ZONES, null, values)
    }

    fun getSafeZones(userId: Int): List<SafeZone> {
        val list = mutableListOf<SafeZone>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SAFE_ZONES,
            null,
            "${DatabaseHelper.COLUMN_ZONE_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SafeZone(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ZONE_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ZONE_NAME)),
                        lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ZONE_LAT)),
                        lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ZONE_LNG)),
                        radius = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ZONE_RADIUS))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteSafeZone(zoneId: Int) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_SAFE_ZONES, "${DatabaseHelper.COLUMN_ZONE_ID} = ?", arrayOf(zoneId.toString()))
    }

    // --- FOG Logging ---

    fun logFogIncident(userId: Int, duration: Int, lat: Double, lng: Double) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FOG_USER_ID, userId)
            put(DatabaseHelper.COLUMN_FOG_TIMESTAMP, System.currentTimeMillis())
            put(DatabaseHelper.COLUMN_FOG_DURATION, duration)
            put(DatabaseHelper.COLUMN_FOG_LAT, lat)
            put(DatabaseHelper.COLUMN_FOG_LNG, lng)
        }
        db.insert(DatabaseHelper.TABLE_FOG, null, values)
    }

    // --- Fall Logging ---

    fun logFallIncident(userId: Int, type: String, lat: Double, lng: Double) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FALL_USER_ID, userId)
            put(DatabaseHelper.COLUMN_FALL_TIMESTAMP, System.currentTimeMillis())
            put(DatabaseHelper.COLUMN_FALL_TYPE, type)
            put(DatabaseHelper.COLUMN_FALL_LAT, lat)
            put(DatabaseHelper.COLUMN_FALL_LNG, lng)
        }
        db.insert(DatabaseHelper.TABLE_FALLS, null, values)
    }
}

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val address: String,
    val isBreach: Boolean
)

data class SafeZone(
    val id: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val radius: Float
)
