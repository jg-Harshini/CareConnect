package com.example.careconnect.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.CaregiverDashboardActivity
import com.example.careconnect.MainActivity
import com.example.careconnect.PatientDashboardActivity
import com.example.careconnect.SettingsActivity
import com.example.careconnect.data.SessionManager
import com.example.careconnect.ui.theme.TextPrimary
import com.example.careconnect.ui.theme.TextSecondary

@Composable
fun AppHeader(
    title: String,
    subtitle: String,
    onBackClick: (() -> Unit)? = null,
    showBackButton: Boolean = false,
    showMenuButton: Boolean = true,
    customMenuItems: (@Composable ColumnScope.(onClose: () -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                sessionManager.logout()
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBackButton && onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        if (showMenuButton) {
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextPrimary)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                ) {
                    val closeMenu = { showMenu = false }
                    
                    // 1. Show custom items if provided (e.g., patient-specific reports/alerts)
                    customMenuItems?.invoke(this, closeMenu)
                    
                    if (customMenuItems != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    // 2. Base Menu Items (Settings & Logout only for main dashboards)
                    DropdownMenuItem(
                        text = { Text("Settings", fontWeight = FontWeight.Medium) },
                        onClick = {
                            closeMenu()
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                        onClick = {
                            closeMenu()
                            showLogoutDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(text = "Logout", fontWeight = FontWeight.ExtraBold, color = TextPrimary) },
        text = { Text("Are you sure you want to logout?", color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun DashboardCard(
    icon: Painter,
    title: String,
    value: String,
    subtitle: String,
    iconContainerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth()
        ) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(14.dp), color = iconContainerColor) {
                Box(contentAlignment = Alignment.Center) { Icon(painter = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.7f))
        }
    }
}
