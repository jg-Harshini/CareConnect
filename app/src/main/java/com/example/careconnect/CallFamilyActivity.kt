package com.example.careconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.careconnect.data.ContactRepository
import com.example.careconnect.data.SessionManager
import com.example.careconnect.ui.CallFamilyScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class CallFamilyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sessionManager = SessionManager(this)
        val contactRepository = ContactRepository(this)
        val userId = sessionManager.getUserId()
        
        // For demo: Add a default contact if none exists
        val contact = contactRepository.getPrimaryContactForUser(userId)
        if (contact == null) {
            contactRepository.addContact(userId, "John (Son)", "1234567890", "Primary Contact")
        }

        setContent {
            CareConnectTheme {
                val primaryContact = remember { contactRepository.getPrimaryContactForUser(userId) }
                
                CallFamilyScreen(
                    contactName = primaryContact?.name ?: "No Contact Set",
                    onBackClicked = { finish() }
                )
            }
        }
    }
}
