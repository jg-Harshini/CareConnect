package com.example.careconnect

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.careconnect.data.*
import com.example.careconnect.ui.ReportsScreen
import com.example.careconnect.ui.theme.CareConnectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PatientReport(
    val distanceTraveled: Double,
    val fogIncidents: Int,
    val fallIncidents: Int,
    val zoneBreaches: Int,
    val medicationAdherence: Int
)

class ReportsActivity : ComponentActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)
        
        // Use passed patient ID or default to current user if not provided (for patient side view)
        val userId = intent.getIntExtra("patient_id", sessionManager.getUserId())

        setContent {
            CareConnectTheme {
                var report by remember { mutableStateOf<PatientReport?>(null) }
                
                LaunchedEffect(userId) {
                    report = generateReport(userId)
                }
                
                ReportsScreen(
                    report = report,
                    onBackClicked = { finish() }
                )
            }
        }
    }

    private suspend fun generateReport(userId: Int): PatientReport = withContext(Dispatchers.IO) {
        val db = databaseHelper.readableDatabase
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)

        // Count FOG
        val fogCursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_FOG} WHERE ${DatabaseHelper.COLUMN_FOG_USER_ID} = ? AND ${DatabaseHelper.COLUMN_FOG_TIMESTAMP} > ?", arrayOf(userId.toString(), sevenDaysAgo.toString()))
        fogCursor.moveToFirst()
        val fogCount = fogCursor.getInt(0)
        fogCursor.close()

        // Count Falls
        val fallCursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_FALLS} WHERE ${DatabaseHelper.COLUMN_FALL_USER_ID} = ? AND ${DatabaseHelper.COLUMN_FALL_TIMESTAMP} > ?", arrayOf(userId.toString(), sevenDaysAgo.toString()))
        fallCursor.moveToFirst()
        val fallCount = fallCursor.getInt(0)
        fallCursor.close()

        // Count Breaches
        val breachCursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_LOCATION_HISTORY} WHERE ${DatabaseHelper.COLUMN_LOC_USER_ID} = ? AND ${DatabaseHelper.COLUMN_LOC_IS_BREACH} = 1 AND ${DatabaseHelper.COLUMN_LOC_TIMESTAMP} > ?", arrayOf(userId.toString(), sevenDaysAgo.toString()))
        breachCursor.moveToFirst()
        val breachCount = breachCursor.getInt(0)
        breachCursor.close()

        // Med Adherence
        val medTaken = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_REMINDERS} WHERE ${DatabaseHelper.COLUMN_REMINDER_USER_ID} = ? AND ${DatabaseHelper.COLUMN_REMINDER_STATUS} = 'TAKEN'", arrayOf(userId.toString()))
        medTaken.moveToFirst()
        val takenCount = medTaken.getInt(0)
        medTaken.close()
        
        val medTotal = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_REMINDERS} WHERE ${DatabaseHelper.COLUMN_REMINDER_USER_ID} = ?", arrayOf(userId.toString()))
        medTotal.moveToFirst()
        val totalCount = medTotal.getInt(0)
        medTotal.close()
        
        val adherence = if (totalCount > 0) (takenCount * 100) / totalCount else 100

        // Calculate actual distance traveled from history
        var totalDistance = 0.0
        val locCursor = db.rawQuery(
            "SELECT ${DatabaseHelper.COLUMN_LOC_LAT}, ${DatabaseHelper.COLUMN_LOC_LNG} FROM ${DatabaseHelper.TABLE_LOCATION_HISTORY} " +
            "WHERE ${DatabaseHelper.COLUMN_LOC_USER_ID} = ? AND ${DatabaseHelper.COLUMN_LOC_TIMESTAMP} > ? " +
            "ORDER BY ${DatabaseHelper.COLUMN_LOC_TIMESTAMP} ASC", 
            arrayOf(userId.toString(), sevenDaysAgo.toString())
        )
        
        if (locCursor.moveToFirst()) {
            var prevLat = locCursor.getDouble(0)
            var prevLng = locCursor.getDouble(1)
            
            while (locCursor.moveToNext()) {
                val currLat = locCursor.getDouble(0)
                val currLng = locCursor.getDouble(1)
                
                val results = FloatArray(1)
                Location.distanceBetween(prevLat, prevLng, currLat, currLng, results)
                totalDistance += results[0]
                
                prevLat = currLat
                prevLng = currLng
            }
        }
        locCursor.close()
        
        // Convert meters to miles
        val distanceMiles = totalDistance * 0.000621371

        PatientReport(
            distanceTraveled = distanceMiles,
            fogIncidents = fogCount,
            fallIncidents = fallCount,
            zoneBreaches = breachCount,
            medicationAdherence = adherence
        )
    }
}
