package com.example.careconnect.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.R
import com.example.careconnect.data.LocationPoint
import com.example.careconnect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertsScreen(
    alerts: List<LocationPoint>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                AppHeader(
                    title = "Alerts History",
                    subtitle = "Recent safety breaches",
                    showBackButton = true,
                    onBackClick = onBackClick,
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
            
            if (alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No safety alerts recorded.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(alerts) { alert ->
                        AlertItem(alert)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertItem(alert: LocationPoint) {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val timeStr = sdf.format(Date(alert.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Safe Zone Breach", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(alert.address, fontSize = 13.sp, color = TextSecondary, maxLines = 1)
                Text(timeStr, fontSize = 12.sp, color = AccentRed, fontWeight = FontWeight.Medium)
            }
        }
    }
}
