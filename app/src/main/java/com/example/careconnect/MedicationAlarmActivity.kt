package com.example.careconnect

import android.media.RingtoneManager
import android.os.Bundle
import android.os.Vibrator
import android.os.VibrationEffect
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.data.ReminderRepository
import com.example.careconnect.ui.theme.CareConnectTheme
import java.text.SimpleDateFormat
import java.util.*

class MedicationAlarmActivity : ComponentActivity() {
    private lateinit var repository: ReminderRepository
    private var ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ReminderRepository(this)
        
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val title = intent.getStringExtra("title") ?: "Medication Reminder"
        val subtitle = intent.getStringExtra("subtitle") ?: "Please take your medicine."
        
        ringtone.play()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
            } else {
                vibrator.vibrate(longArrayOf(0, 500, 500), 0)
            }
        }

        setContent {
            CareConnectTheme {
                Column(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9)).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Medication, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(title, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                    Text(subtitle, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = {
                            ringtone.stop()
                            vibrator.cancel()
                            if (reminderId != -1) {
                                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                repository.updateReminderStatus(reminderId, "TAKEN", date)
                            }
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("I HAVE TAKEN IT", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
