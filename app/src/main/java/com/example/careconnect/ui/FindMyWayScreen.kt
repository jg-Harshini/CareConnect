package com.example.careconnect.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.data.LocationRepository
import com.example.careconnect.data.SafeZone
import com.example.careconnect.ui.theme.*
import org.osmdroid.util.GeoPoint

@Composable
fun FindMyWayScreen(
    patientId: Int,
    currentLat: Double,
    currentLng: Double,
    homeLat: Double,
    homeLng: Double,
    currentAddress: String = "Locating...",
    distanceToHome: String = "0.0",
    onBackClicked: () -> Unit = {},
    onMarkHomeClicked: (Double, Double) -> Unit = { _, _ -> },
    onImHomeSafeClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { LocationRepository(context) }
    var safeZones by remember { mutableStateOf(listOf<SafeZone>()) }
    var showSetHomeConfirm by remember { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(patientId) {
        safeZones = repository.getSafeZones(patientId)
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                AppHeader(
                    title = "Find My Way",
                    subtitle = "Navigate back home safely",
                    showBackButton = true,
                    onBackClick = onBackClicked,
                    showMenuButton = false
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Tap map to set home location",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Map Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .shadow(4.dp)
            ) {
                OSMMapView(
                    latitude = currentLat, 
                    longitude = currentLng, 
                    modifier = Modifier.fillMaxSize(),
                    destinationLat = homeLat,
                    destinationLng = homeLng,
                    onMapClick = { point -> showSetHomeConfirm = point },
                    circles = safeZones.map { GeofenceCircle(it.lat, it.lng, it.radius, it.name) },
                    showHomeIcon = true
                )
                
                // Info Overlay
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Distance to Home", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                if (homeLat == 0.0) "Home not set" else "$distanceToHome miles", 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.Black, 
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onImHomeSafeClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, SecondaryTeal),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryTeal)
            ) {
                Text("I'm Home Safe", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    showSetHomeConfirm?.let { point ->
        AlertDialog(
            onDismissRequest = { showSetHomeConfirm = null },
            title = { Text("Set Home Location") },
            text = { Text("Do you want to set this point as your home?") },
            confirmButton = {
                Button(onClick = {
                    onMarkHomeClicked(point.latitude, point.longitude)
                    showSetHomeConfirm = null
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showSetHomeConfirm = null }) { Text("Cancel") }
            }
        )
    }
}
