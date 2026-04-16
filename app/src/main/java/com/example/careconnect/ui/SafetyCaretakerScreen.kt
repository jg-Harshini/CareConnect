package com.example.careconnect.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.CaregiverDashboardActivity
import com.example.careconnect.R
import com.example.careconnect.ui.theme.CareConnectTheme

@Composable
fun SafetyCaretakerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FB))
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            item {
                AppHeader(
                    title = stringResource(id = R.string.safety_zones),
                    subtitle = stringResource(id = R.string.geofence_monitoring),
                    showBackButton = true,
                    onBackClick = {
                        context.startActivity(Intent(context, CaregiverDashboardActivity::class.java))
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                SafetyAllSafeCard()
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(id = R.string.active_safe_zones),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SafeZoneCard(
                    icon = painterResource(id = R.drawable.ic_location),
                    title = stringResource(id = R.string.home),
                    address = stringResource(id = R.string.home_address),
                    status = stringResource(id = R.string.active),
                    statusColor = Color(0xFFDCFCE7),
                    statusTextColor = Color(0xFF166534),
                    progress = { 1f },
                    progressColor = Color(0xFF22C55E),
                    inside = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                SafeZoneCard(
                    icon = painterResource(id = R.drawable.ic_location),
                    title = stringResource(id = R.string.oakwood_park),
                    address = stringResource(id = R.string.oakwood_address),
                    status = stringResource(id = R.string.inactive),
                    statusColor = Color(0xFFF1F5F9),
                    statusTextColor = Color(0xFF64748B),
                    progress = { 0.3f },
                    progressColor = Color(0xFFE2E8F0),
                    inside = false
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                SafeZoneCard(
                    icon = painterResource(id = R.drawable.ic_location),
                    title = stringResource(id = R.string.community_center),
                    address = stringResource(id = R.string.community_address),
                    status = stringResource(id = R.string.inactive),
                    statusColor = Color(0xFFE0E7FF),
                    statusTextColor = Color(0xFF4338CA),
                    progress = { 0f },
                    progressColor = Color(0xFFE2E8F0),
                    inside = false,
                    iconTint = Color(0xFF6366F1)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SafetyAllSafeCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00C853))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield),
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.all_safe),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.patient_within_safe_zones),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.White.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

@Composable
fun SafeZoneCard(
    icon: Painter,
    title: String,
    address: String,
    status: String,
    statusColor: Color,
    statusTextColor: Color,
    progress: () -> Float,
    progressColor: Color,
    inside: Boolean,
    modifier: Modifier = Modifier,
    iconTint: Color = statusTextColor
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )
                        Text(
                            text = address,
                            fontSize = 14.sp,
                            color = Color(0xFF676E76)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor
                ) {
                    Text(
                        text = status,
                        color = statusTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = progress(),
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = Color(0xFFF1F5F9)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (inside) stringResource(id = R.string.inside) else stringResource(id = R.string.outside),
                    fontSize = 14.sp,
                    color = Color(0xFF676E76)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SafetyCaretakerScreenPreview() {
    CareConnectTheme {
        SafetyCaretakerScreen()
    }
}
