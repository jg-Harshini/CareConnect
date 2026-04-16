package com.example.careconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.careconnect.data.SessionManager
import com.example.careconnect.data.UserRepository
import com.example.careconnect.ui.LinkAccountScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class LinkAccountActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        userRepository = UserRepository(this)
        sessionManager = SessionManager(this)
        val caretakerId = sessionManager.getUserId()

        setContent {
            CareConnectTheme {
                LinkAccountScreen(
                    onLinkSubmit = { patientId ->
                        if (patientId.isEmpty()) {
                            Toast.makeText(this, "Please enter a Patient ID", Toast.LENGTH_SHORT).show()
                        } else {
                            val success = userRepository.linkPatientToCaretaker(patientId, caretakerId)
                            if (success) {
                                Toast.makeText(this, "Patient linked successfully!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Invalid Patient ID or Patient already linked.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onBackClicked = { finish() }
                )
            }
        }
    }
}
