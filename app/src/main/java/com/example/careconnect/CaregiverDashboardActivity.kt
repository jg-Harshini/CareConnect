package com.example.careconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.careconnect.data.*
import com.example.careconnect.ui.CaregiverDashboardScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class CaregiverDashboardActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        sessionManager = SessionManager(this)
        userRepository = UserRepository(this)
        
        val caretakerId = sessionManager.getUserId()

        setContent {
            CareConnectTheme {
                var patients by remember { mutableStateOf<List<User>>(emptyList()) }
                var caretakerName by remember { mutableStateOf("") }
                
                LaunchedEffect(caretakerId) {
                    caretakerName = sessionManager.getUserName()
                    patients = userRepository.getPatientsForCaretaker(caretakerId)
                }

                CaregiverDashboardScreen(
                    caretakerName = caretakerName,
                    patients = patients,
                    onPatientClick = { patient ->
                        // Navigate to safety/monitoring for this specific patient
                        val intent = Intent(this, SafetyActivity::class.java).apply {
                            putExtra("patient_id", patient.id)
                            putExtra("patient_name", patient.name)
                        }
                        startActivity(intent)
                    },
                    onLinkPatientClick = {
                        startActivity(Intent(this, LinkAccountActivity::class.java))
                    },
                    onLogoutClick = {
                        handleLogout()
                    }
                )
            }
        }
    }

    private fun handleLogout() {
        sessionManager.logout()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
