package com.example.careconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.careconnect.data.SessionManager
import com.example.careconnect.data.UserRepository
import com.example.careconnect.ui.LoginScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class LoginActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        userRepository = UserRepository(this)
        sessionManager = SessionManager(this)

        setContent {
            CareConnectTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            val user = userRepository.loginUser(email, password)
                            if (user != null) {
                                sessionManager.saveUserSession(user.id, user.name, user.role)
                                navigateToDashboard(user.role)
                            } else {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }

    private fun navigateToDashboard(role: String) {
        val intent = if (role == "patient") {
            Intent(this, PatientDashboardActivity::class.java)
        } else {
            Intent(this, CaregiverDashboardActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
