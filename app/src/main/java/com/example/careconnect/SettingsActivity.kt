package com.example.careconnect

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.data.SessionManager
import com.example.careconnect.data.UserRepository
import com.example.careconnect.ui.theme.CareConnectTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)
        val userRole = sessionManager.getUserRole()
        val userName = sessionManager.getUserName()

        setContent {
            CareConnectTheme {
                SettingsScreen(
                    role = userRole,
                    userName = userName,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(role: String, userName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("CareConnectPrefs", Context.MODE_PRIVATE)

    var sosNumber by remember { 
        mutableStateOf(sharedPreferences.getString("sos_number", "") ?: "") 
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Section (Common)
            SettingsGroup(title = "Profile") {
                Text("Name: $userName", style = MaterialTheme.typography.bodyLarge)
                Text("Role: ${role.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }

            if (role == "caretaker") {
                // Caretaker Specific Settings
                SettingsGroup(title = "Monitoring Preferences") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Instant Breach Notifications")
                        Switch(checked = true, onCheckedChange = {})
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Daily Health Summaries")
                        Switch(checked = true, onCheckedChange = {})
                    }
                }
            } else {
                // Patient Specific Settings
                SettingsGroup(title = "Emergency Contact") {
                    OutlinedTextField(
                        value = sosNumber,
                        onValueChange = { sosNumber = it },
                        label = { Text("SOS Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Text(
                        "This number will be alerted if a fall or FOG is detected.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                SettingsGroup(title = "Safety Features") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Automatic Fall Detection")
                        Switch(checked = true, onCheckedChange = {})
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("FOG Metronome Assistance")
                        Switch(checked = true, onCheckedChange = {})
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    sharedPreferences.edit().apply {
                        putString("sos_number", sosNumber)
                        apply()
                    }
                    Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save Changes")
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}
