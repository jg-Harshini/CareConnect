package com.example.careconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.careconnect.ui.SafetyCaretakerScreen
import com.example.careconnect.ui.theme.CareConnectTheme

class SafetyCaretakerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareConnectTheme {
                SafetyCaretakerScreen()
            }
        }
    }
}
