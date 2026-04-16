package com.example.careconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.careconnect.data.Reminder
import com.example.careconnect.data.ReminderRepository
import com.example.careconnect.data.SessionManager
import com.example.careconnect.ui.RemindersScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class RemindersActivity : ComponentActivity() {
    private lateinit var repository: ReminderRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        repository = ReminderRepository(this)
        sessionManager = SessionManager(this)
        
        // If caretaker opens this, they pass the patient_id
        val currentUserId = sessionManager.getUserId()
        val userRole = sessionManager.getUserRole()
        val targetPatientId = intent.getIntExtra("patient_id", -1)
        
        // Determine whose reminders we are managing
        val userIdToManage = if (userRole == "caretaker" && targetPatientId != -1) {
            targetPatientId
        } else {
            currentUserId
        }

        setContent {
            CareConnectTheme {
                var reminders by remember { mutableStateOf(listOf<Reminder>()) }
                
                LaunchedEffect(userIdToManage) {
                    reminders = repository.getRemindersForUser(userIdToManage)
                }

                RemindersScreen(
                    reminders = reminders,
                    canEdit = userRole == "caretaker", // Only caretaker can add/delete
                    onBackClicked = { finish() },
                    onAddReminder = { name, dose, time ->
                        repository.addReminder(userIdToManage, name, dose, time)
                        reminders = repository.getRemindersForUser(userIdToManage)
                        Toast.makeText(this, "Medication Added", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteReminder = { id ->
                        repository.deleteReminder(id)
                        reminders = repository.getRemindersForUser(userIdToManage)
                        Toast.makeText(this, "Medication Removed", Toast.LENGTH_SHORT).show()
                    },
                    onStatusToggle = { id, taken ->
                        // Patient marks as taken
                        val status = if (taken) "TAKEN" else "PENDING"
                        repository.updateReminderStatus(id, status)
                        reminders = repository.getRemindersForUser(userIdToManage)
                    }
                )
            }
        }
    }
}
