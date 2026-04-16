package com.example.careconnect.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.careconnect.data.LocationRepository
import com.example.careconnect.data.SafeZone
import com.example.careconnect.ui.theme.*
import com.google.android.gms.location.LocationServices

@Composable
fun SafeZoneScreen(
    patientId: Int,
    onBackClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { LocationRepository(context) }
    var safeZones by remember { mutableStateOf(listOf<SafeZone>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Load safe zones
    LaunchedEffect(Unit) {
        safeZones = repository.getSafeZones(patientId)
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                AppHeader(
                    title = "Safe Zones",
                    subtitle = "Define safety boundaries",
                    showBackButton = true,
                    onBackClick = onBackClicked,
                    showMenuButton = false
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Safe Zone")
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Active Boundaries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (safeZones.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "No safe zones defined yet.\nTap + to add a boundary around home or frequent places.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(safeZones) { zone ->
                        SafeZoneItem(zone) {
                            repository.deleteSafeZone(zone.id)
                            safeZones = repository.getSafeZones(patientId)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSafeZoneDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, radius ->
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            repository.saveSafeZone(patientId, name, location.latitude, location.longitude, radius)
                            safeZones = repository.getSafeZones(patientId)
                            showAddDialog = false
                        } else {
                            Toast.makeText(context, "Could not get current location. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Location permission required to set safe zone at current position", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun SafeZoneItem(zone: SafeZone, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = PrimaryLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocationOn, null, tint = PrimaryBlue)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("${zone.radius.toInt()} meter radius", fontSize = 14.sp, color = TextSecondary)
                Text(String.format("%.4f, %.4f", zone.lat, zone.lng), fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.7f))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun AddSafeZoneDialog(onDismiss: () -> Unit, onSave: (String, Float) -> Unit) {
    var name by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Safe Zone", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Define a safe area around the patient's current location.", color = TextSecondary)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Zone Name (e.g. Home)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Radius (meters)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty()) onSave(name, radius.toFloatOrNull() ?: 200f) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Create Zone")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
