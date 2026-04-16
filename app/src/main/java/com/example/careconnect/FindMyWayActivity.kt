package com.example.careconnect

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import com.example.careconnect.data.SessionManager
import com.example.careconnect.data.UserRepository
import com.example.careconnect.ui.FindMyWayScreen
import com.example.careconnect.ui.theme.CareConnectTheme
import com.google.android.gms.location.LocationServices

class FindMyWayActivity : ComponentActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository
    
    private var currentAddress by mutableStateOf("Locating...")
    private var currentLat by mutableDoubleStateOf(0.0)
    private var currentLng by mutableDoubleStateOf(0.0)
    private var homeLat by mutableDoubleStateOf(0.0)
    private var homeLng by mutableDoubleStateOf(0.0)
    private var distanceToHome by mutableStateOf("0.0")

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                val lat = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0.0)
                val lng = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0.0)
                
                if (lat != 0.0) {
                    currentAddress = intent.getStringExtra(LocationService.EXTRA_ADDRESS) ?: "Unknown Location"
                    currentLat = lat
                    currentLng = lng
                    updateDistance()
                }
            }
        }
    }

    private fun updateDistance() {
        if (currentLat != 0.0 && homeLat != 0.0) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(currentLat, currentLng, homeLat, homeLng, results)
            val miles = results[0] * 0.000621371f
            distanceToHome = String.format("%.2f", miles)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        sessionManager = SessionManager(this)
        userRepository = UserRepository(this)
        val userId = sessionManager.getUserId()

        // 1. Load saved home location from DB
        val user = userRepository.getUserById(userId)
        if (user != null) {
            homeLat = user.homeLat
            homeLng = user.homeLng
        }

        // 2. Try to get last known location immediately so map isn't blank
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLng = location.longitude
                    updateDistance()
                }
            }
        }

        setContent {
            CareConnectTheme {
                FindMyWayScreen(
                    patientId = userId,
                    currentLat = currentLat,
                    currentLng = currentLng,
                    homeLat = homeLat,
                    homeLng = homeLng,
                    currentAddress = currentAddress,
                    distanceToHome = distanceToHome,
                    onBackClicked = { finish() },
                    onMarkHomeClicked = { lat, lng ->
                        userRepository.setHomeLocation(userId, lat, lng)
                        // Update local state so UI reacts immediately
                        homeLat = lat
                        homeLng = lng
                        updateDistance()
                        Toast.makeText(this, "Home location updated!", Toast.LENGTH_SHORT).show()
                    },
                    onImHomeSafeClicked = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(locationReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationReceiver)
    }
}
