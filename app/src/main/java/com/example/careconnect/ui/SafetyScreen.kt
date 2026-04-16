package com.example.careconnect.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.AlertsActivity
import com.example.careconnect.LocationService
import com.example.careconnect.R
import com.example.careconnect.ReportsActivity
import com.example.careconnect.RemindersActivity
import com.example.careconnect.data.LocationRepository
import com.example.careconnect.data.SafeZone
import com.example.careconnect.ui.theme.*
import org.osmdroid.util.GeoPoint

@Composable
fun SafetyScreen(
    patientId: Int,
    patientName: String = "Patient",
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { LocationRepository(context) }
    
    var currentAddress by remember { mutableStateOf("Locating...") }
    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLng by remember { mutableDoubleStateOf(0.0) }
    var isSafe by remember { mutableStateOf(true) }
    
    var safeZones by remember { mutableStateOf(listOf<SafeZone>()) }
    var showAddZoneDialog by remember { mutableStateOf<GeoPoint?>(null) }

    // 1. Initial Load: Fetch last known location and zones
    LaunchedEffect(patientId) {
        if (patientId == -1) {
            Toast.makeText(context, "Error: Invalid Patient Selection", Toast.LENGTH_LONG).show()
            return@LaunchedEffect
        }
        
        safeZones = repository.getSafeZones(patientId)
        
        // Fetch last location from history
        val history = repository.getHistoryForUser(patientId, limit = 1)
        if (history.isNotEmpty()) {
            val lastPoint = history[0]
            currentLat = lastPoint.latitude
            currentLng = lastPoint.longitude
            currentAddress = lastPoint.address
            isSafe = !lastPoint.isBreach
        }
    }

    // 2. Live Updates: Listen for new location broadcasts
    DisposableEffect(patientId) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                    val lat = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0.0)
                    val lng = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0.0)
                    val address = intent.getStringExtra(LocationService.EXTRA_ADDRESS) ?: "Unknown"
                    
                    if (lat != 0.0) {
                        currentLat = lat
                        currentLng = lng
                        currentAddress = address
                        
                        // Check if safe locally
                        var pointInZone = false
                        if (safeZones.isEmpty()) {
                            pointInZone = true 
                        } else {
                            for (zone in safeZones) {
                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(lat, lng, zone.lat, zone.lng, results)
                                if (results[0] <= zone.radius) {
                                    pointInZone = true
                                    break
                                }
                            }
                        }
                        isSafe = pointInZone
                    }
                }
            }
        }
        val filter = IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                AppHeader(
                    title = "Safety Details",
                    subtitle = patientName,
                    showBackButton = true,
                    onBackClick = onBackClick,
                    customMenuItems = { onClose ->
                        DropdownMenuItem(
                            text = { Text("Health Reports") },
                            onClick = {
                                onClose()
                                val intent = Intent(context, ReportsActivity::class.java).apply {
                                    putExtra("patient_id", patientId)
                                }
                                context.startActivity(intent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Alerts History") },
                            onClick = {
                                onClose()
                                val intent = Intent(context, AlertsActivity::class.java).apply {
                                    putExtra("patient_id", patientId)
                                }
                                context.startActivity(intent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Medications") },
                            onClick = {
                                onClose()
                                val intent = Intent(context, RemindersActivity::class.java).apply {
                                    putExtra("patient_id", patientId)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                )
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Live Tracking (Tap map to add Safe Zone)",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                OSMMapView(
                    latitude = currentLat,
                    longitude = currentLng,
                    modifier = Modifier.fillMaxSize(),
                    onMapClick = { point -> showAddZoneDialog = point },
                    circles = safeZones.map { GeofenceCircle(it.lat, it.lng, it.radius, it.name) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSafe) SecondaryTeal else AccentRed)
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(if (isSafe) "Secure" else "BREACH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(currentAddress, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    showAddZoneDialog?.let { point ->
        AddGeofenceDialog(
            onDismiss = { showAddZoneDialog = null },
            onSave = { name, radius ->
                if (patientId != -1) {
                    repository.saveSafeZone(patientId, name, point.latitude, point.longitude, radius)
                    safeZones = repository.getSafeZones(patientId)
                    showAddZoneDialog = null
                    Toast.makeText(context, "Safe Zone Added: $name", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Cannot save: No patient selected", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun AddGeofenceDialog(onDismiss: () -> Unit, onSave: (String, Float) -> Unit) {
    var name by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("500") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Safe Zone", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Zone Name") }, 
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = radius, 
                    onValueChange = { radius = it }, 
                    label = { Text("Radius (meters)") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { 
                    val rad = radius.toFloatOrNull() ?: 500f
                    onSave(name.trim(), rad) 
                }
            ) {
                Text("Save Zone")
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancel") } 
        }
    )
}
