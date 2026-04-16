package com.example.careconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.careconnect.data.UserRepository
import com.example.careconnect.ui.RegisterScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class RegisterActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        userRepository = UserRepository(this)

        setContent {
            CareConnectTheme {
                RegisterScreen(
                    onRegisterClick = { name, email, password, role ->
                        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            val result = userRepository.registerUser(name, email, password, role)
                            if (result != -1L) {
                                Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                                // Navigate back to LoginActivity
                                finish() 
                            } else {
                                Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onLoginClick = {
                        finish() // Go back to login
                    }
                )
            }
        }
    }
}
