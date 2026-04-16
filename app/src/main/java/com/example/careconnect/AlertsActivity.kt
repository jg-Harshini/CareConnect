package com.example.careconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.careconnect.data.LocationPoint
import com.example.careconnect.data.LocationRepository
import com.example.careconnect.data.SessionManager
import com.example.careconnect.ui.AlertsScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class AlertsActivity : ComponentActivity() {
    private lateinit var locationRepository: LocationRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        locationRepository = LocationRepository(this)
        sessionManager = SessionManager(this)
        
        val userRole = sessionManager.getUserRole()
        val currentUserId = sessionManager.getUserId()
        
        // If caretaker, they are viewing alerts for a specific patient
        // If patient, they view their own alerts
        val targetPatientId = intent.getIntExtra("patient_id", -1)
        val userIdToView = if (userRole == "caretaker" && targetPatientId != -1) {
            targetPatientId
        } else {
            currentUserId
        }

        setContent {
            CareConnectTheme {
                var alerts by remember { mutableStateOf(listOf<LocationPoint>()) }
                
                LaunchedEffect(userIdToView) {
                    alerts = locationRepository.getHistoryForUser(userIdToView).filter { it.isBreach }
                }

                AlertsScreen(
                    alerts = alerts,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
