package com.example.careconnect.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.LocationService
import com.example.careconnect.R
import com.example.careconnect.data.User
import com.example.careconnect.ui.theme.*

@Composable
fun PatientDashboardScreen(
    userName: String,
    patientId: String,
    linkedCaretaker: User?,
    onSosClick: () -> Unit,
    onCallFamilyClick: () -> Unit,
    onMedicationClick: () -> Unit,
    onFindWayClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    var currentAddress by remember { mutableStateOf("Locating...") }
    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLng by remember { mutableDoubleStateOf(0.0) }
    val clipboardManager = LocalClipboardManager.current

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                    currentAddress = intent.getStringExtra(LocationService.EXTRA_ADDRESS) ?: "Unknown"
                    currentLat = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0.0)
                    currentLng = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0.0)
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
                    title = "Hello, $userName",
                    subtitle = if (linkedCaretaker != null) "Linked to ${linkedCaretaker.name}" else "Not Linked",
                    showMenuButton = true
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
            
            PatientLiveMapCard(currentLat, currentLng, currentAddress)
            
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("My Unique ID (Share with Caretaker)", fontSize = 12.sp, color = TextSecondary)
                        Text(text = patientId, fontSize = 20.sp, fontWeight = FontWeight.Black, color = PrimaryBlue, letterSpacing = 2.sp)
                    }
                    Button(
                        onClick = { clipboardManager.setText(AnnotatedString(patientId)) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight, contentColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            EmergencySosCard(onSosClick = onSosClick)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Support Tools", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 16.dp))
            
            // Fixed Alignment Support Tools (Removed Voice Help)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Medication",
                    icon = painterResource(id = R.drawable.ic_meds),
                    iconContainerColor = Color(0xFFD9F9E6),
                    iconTintColor = SecondaryTeal,
                    onClick = onMedicationClick
                )
                DashboardActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Go Home",
                    icon = painterResource(id = R.drawable.ic_location),
                    iconContainerColor = Color(0xFFF2E7FF),
                    iconTintColor = AccentPurple,
                    onClick = onFindWayClick
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DashboardActionCard(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                title = "Call Caretaker",
                icon = painterResource(id = R.drawable.ic_phone),
                iconContainerColor = Color(0xFFFFEBD5),
                iconTintColor = AccentOrange,
                onClick = onCallFamilyClick
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PatientLiveMapCard(lat: Double, lng: Double, address: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFF0F4F8)),
                contentAlignment = Alignment.Center
            ) {
                if (lat != 0.0 && lng != 0.0) {
                    OSMMapView(latitude = lat, longitude = lng, modifier = Modifier.fillMaxSize())
                } else {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = PrimaryLight) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, null, tint = PrimaryBlue, modifier = Modifier.size(24.dp)) }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Current Location", fontSize = 12.sp, color = TextSecondary)
                    Text(text = address, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun EmergencySosCard(modifier: Modifier = Modifier, onSosClick: () -> Unit = {}) {
    Card(
        onClick = onSosClick,
        modifier = modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color(0xFFFF5F6D), Color(0xFFFF2D55)))), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(painter = painterResource(id = R.drawable.ic_alerts), contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "EMERGENCY SOS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun DashboardActionCard(title: String, icon: Painter, iconContainerColor: Color, iconTintColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = iconContainerColor) {
                Box(contentAlignment = Alignment.Center) { Icon(painter = icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(24.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
