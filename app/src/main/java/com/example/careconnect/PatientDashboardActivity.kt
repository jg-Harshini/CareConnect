package com.example.careconnect

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.careconnect.data.SessionManager
import com.example.careconnect.data.User
import com.example.careconnect.data.UserRepository
import com.example.careconnect.ui.PatientDashboardScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class PatientDashboardActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startLocationService()
        } else {
            Toast.makeText(this, "Location and SMS permissions are required for safety", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        sessionManager = SessionManager(this)
        userRepository = UserRepository(this)
        val userId = sessionManager.getUserId()
        val userName = sessionManager.getUserName()

        checkAndRequestPermissions()

        setContent {
            CareConnectTheme {
                var currentUser by remember { mutableStateOf<User?>(null) }
                var linkedCaretaker by remember { mutableStateOf<User?>(null) }
                
                LaunchedEffect(userId) {
                    currentUser = userRepository.getUserById(userId)
                    linkedCaretaker = userRepository.getLinkedUser(userId)
                }

                PatientDashboardScreen(
                    userName = userName,
                    patientId = currentUser?.patientUniqueId ?: "Generating...",
                    linkedCaretaker = linkedCaretaker,
                    onSosClick = { sendSos() },
                    onCallFamilyClick = { handleCaretakerCall() },
                    onMedicationClick = { startActivity(Intent(this, RemindersActivity::class.java)) },
                    onFindWayClick = { startActivity(Intent(this, FindMyWayActivity::class.java)) },
                    onLogoutClick = { handleLogout() }
                )
            }
        }
    }

    private fun handleCaretakerCall() {
        val sharedPrefs = getSharedPreferences("CareConnectPrefs", Context.MODE_PRIVATE)
        val sosNumber = sharedPrefs.getString("sos_number", "")
        
        if (!sosNumber.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$sosNumber")
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "No caretaker number set in Settings", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            startLocationService()
        } else {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
    }

    private fun sendSos() {
        val intent = Intent(this, LocationService::class.java)
        intent.action = LocationService.ACTION_SEND_SOS
        startService(intent)
        Toast.makeText(this, "Emergency SOS Broadcast Sent!", Toast.LENGTH_LONG).show()
    }

    private fun handleLogout() {
        stopService(Intent(this, LocationService::class.java))
        sessionManager.logout()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
