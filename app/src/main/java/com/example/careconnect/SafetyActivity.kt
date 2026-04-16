package com.example.careconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.careconnect.ui.SafetyScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class SafetyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val patientId = intent.getIntExtra("patient_id", -1)
        val patientName = intent.getStringExtra("patient_name") ?: "Patient"

        setContent {
            CareConnectTheme {
                SafetyScreen(
                    patientId = patientId,
                    patientName = patientName,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
