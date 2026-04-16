package com.example.careconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.PatientReport
import com.example.careconnect.ui.theme.*

@Composable
fun ReportsScreen(
    report: PatientReport?,
    onBackClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                AppHeader(
                    title = "Health Report",
                    subtitle = "Last 7 days summary",
                    showBackButton = true,
                    onBackClick = onBackClicked,
                    showMenuButton = false
                )
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        if (report == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                item {
                    ReportStatCard(
                        title = "Medication Adherence",
                        value = "${report.medicationAdherence}%",
                        icon = Icons.Default.Medication,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                item {
                    ReportStatCard(
                        title = "FOG Incidents",
                        value = "${report.fogIncidents}",
                        icon = Icons.Default.DirectionsWalk,
                        color = Color(0xFFFF9800)
                    )
                }
                
                item {
                    ReportStatCard(
                        title = "Fall Incidents",
                        value = "${report.fallIncidents}",
                        icon = Icons.Default.Warning,
                        color = Color.Red
                    )
                }
                
                item {
                    ReportStatCard(
                        title = "Safe Zone Breaches",
                        value = "${report.zoneBreaches}",
                        icon = Icons.Default.Map,
                        color = PrimaryBlue
                    )
                }
                
                item {
                    ReportStatCard(
                        title = "Est. Distance",
                        value = "${report.distanceTraveled} km",
                        icon = Icons.Default.Timeline,
                        color = Color(0xFF9C27B0)
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun ReportStatCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(title, color = TextSecondary, fontSize = 14.sp)
                Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = TextPrimary)
            }
        }
    }
}
