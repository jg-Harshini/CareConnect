package com.example.careconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.R
import com.example.careconnect.data.User
import com.example.careconnect.ui.theme.*

@Composable
fun CaregiverDashboardScreen(
    caretakerName: String,
    patients: List<User>,
    onPatientClick: (User) -> Unit,
    onLinkPatientClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                AppHeader(
                    title = "Hi, $caretakerName",
                    subtitle = "Caregiver Mode",
                    showMenuButton = true
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onLinkPatientClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, null) },
                text = { Text("Link New Patient") }
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Summary Section (Dynamic)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    icon = painterResource(id = R.drawable.ic_shield),
                    title = "Active Patients",
                    value = patients.size.toString(),
                    subtitle = "Monitoring active",
                    iconContainerColor = PrimaryLight,
                    iconTint = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    icon = painterResource(id = R.drawable.ic_alerts),
                    title = "Recent Alerts",
                    value = "0", // Placeholder for dynamic count
                    subtitle = "Last 24 hours",
                    iconContainerColor = Color(0xFFFFF0F0),
                    iconTint = AccentRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "My Managed Profiles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (patients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PeopleOutline, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No patients linked yet", fontWeight = FontWeight.Medium, color = TextSecondary)
                        TextButton(onClick = onLinkPatientClick) {
                            Text("Start by linking a patient ID", color = PrimaryBlue)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(patients) { patient ->
                        CaretakerPatientItem(patient = patient, onClick = { onPatientClick(patient) })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CaretakerPatientItem(patient: User, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = SecondaryLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = SecondaryTeal, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(patient.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text("Patient ID: ${patient.patientUniqueId}", fontSize = 13.sp, color = TextSecondary, letterSpacing = 1.sp)
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFE8F5E9),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
