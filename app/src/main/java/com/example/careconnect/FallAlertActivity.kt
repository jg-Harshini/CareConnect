package com.example.careconnect

import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.data.LocationRepository
import com.example.careconnect.data.SessionManager
import com.example.careconnect.ui.theme.CareConnectTheme

class FallAlertActivity : ComponentActivity() {
    private lateinit var locationRepository: LocationRepository
    private lateinit var sessionManager: SessionManager
    private var ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationRepository = LocationRepository(this)
        sessionManager = SessionManager(this)
        
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)
        
        ringtone.play()

        setContent {
            CareConnectTheme {
                FallAlertScreen(
                    onIAmOkay = {
                        ringtone.stop()
                        locationRepository.logFallIncident(sessionManager.getUserId(), "RESOLVED", lat, lng)
                        finish()
                    },
                    onTimeout = {
                        ringtone.stop()
                        locationRepository.logFallIncident(sessionManager.getUserId(), "CRITICAL", lat, lng)
                        // In a real app, send a network request or high-priority broadcast to caretaker here
                        finish()
                    }
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (ringtone.isPlaying) ringtone.stop()
    }
}

@Composable
fun FallAlertScreen(onIAmOkay: () -> Unit, onTimeout: () -> Unit) {
    var timeLeft by remember { mutableStateOf(15) }
    
    LaunchedEffect(Unit) {
        object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
            }
            override fun onFinish() {
                onTimeout()
            }
        }.start()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFEBEE))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = Color.Red
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Fall Detected!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.Red
        )
        
        Text(
            "Are you okay? We will notify your caretaker in $timeLeft seconds.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onIAmOkay,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("I AM OKAY", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
