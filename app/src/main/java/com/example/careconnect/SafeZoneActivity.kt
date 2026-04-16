package com.example.careconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.careconnect.data.SessionManager
import com.example.careconnect.ui.SafeZoneScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class SafeZoneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sessionManager = SessionManager(this)
        val linkedPatientId = sessionManager.getUserId() // For now, in demo, use current user or linked ID

        setContent {
            CareConnectTheme {
                SafeZoneScreen(
                    patientId = linkedPatientId,
                    onBackClicked = { finish() }
                )
            }
        }
    }
}
