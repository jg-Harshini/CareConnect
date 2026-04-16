package com.example.careconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.data.Reminder
import com.example.careconnect.ui.theme.*

@Composable
fun RemindersScreen(
    reminders: List<Reminder>,
    canEdit: Boolean,
    onBackClicked: () -> Unit,
    onAddReminder: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteReminder: (Int) -> Unit = {},
    onStatusToggle: (Int, Boolean) -> Unit = { _, _ -> }
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                AppHeader(
                    title = "Medication",
                    subtitle = if (canEdit) "Manage schedules" else "My daily doses",
                    showBackButton = true,
                    onBackClick = onBackClicked,
                    showMenuButton = false
                )
            }
        },
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Add Medication")
                }
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            if (reminders.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(0.7f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No medications scheduled.", color = TextSecondary)
                    }
                }
            } else {
                items(reminders) { reminder ->
                    ReminderListItem(
                        reminder = reminder, 
                        canDelete = canEdit,
                        canToggle = !canEdit,
                        onDelete = { onDeleteReminder(reminder.id) },
                        onToggle = { onStatusToggle(reminder.id, it) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddMedicationDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, dose, time ->
                onAddReminder(name, dose, time)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderListItem(
    reminder: Reminder, 
    canDelete: Boolean,
    canToggle: Boolean,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val isTaken = reminder.status == "TAKEN"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) Color(0xFFF1F8E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTaken) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isTaken) Color(0xFFC8E6C9) else Color(0xFFE8F5E9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Medication, 
                        null, 
                        tint = if (isTaken) Color(0xFF1B5E20) else Color(0xFF2E7D32)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp,
                    textDecoration = if (isTaken) TextDecoration.LineThrough else null,
                    color = if (isTaken) TextSecondary else TextPrimary
                )
                Text(reminder.subtitle, color = TextSecondary, fontSize = 14.sp)
                Text(reminder.time, fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryBlue)
            }
            
            if (canToggle) {
                Checkbox(
                    checked = isTaken,
                    onCheckedChange = { onToggle(it) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E7D32))
                )
            }
            
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun AddMedicationDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medication", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Medication Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dose, 
                    onValueChange = { dose = it }, 
                    label = { Text("Dosage (e.g. 500mg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time, 
                    onValueChange = { time = it }, 
                    label = { Text("Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if(name.isNotEmpty()) onSave(name, dose, time) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
