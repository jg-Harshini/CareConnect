package com.example.careconnect

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.careconnect.data.*
import com.google.android.gms.location.*
import java.util.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import kotlin.math.*

class LocationService : Service(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val channelId = "CareConnectMonitoringChannel"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private lateinit var locationRepository: LocationRepository
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var sensorManager: SensorManager
    
    // --- Fall Detection State ---
    private val FALL_THRESHOLD = 25.0f
    private val FREE_FALL_THRESHOLD = 5.0f
    private val FREE_FALL_DURATION = 100L
    private val IMPACT_WINDOW = 500L
    private val STILL_MIN = 8.5f
    private val STILL_MAX = 11.5f
    private val STILL_DURATION = 1500L
    private val FALL_COOLDOWN = 10000L

    private var freeFallStartTime = 0L
    private var lastImpactTime = 0L
    private var lastFallAlertTime = 0L
    private var stillnessStartTime = 0L
    private var isMonitoringStillness = false

    // --- FOG Detection State ---
    private val FOG_MIN_HZ = 3.0f
    private val FOG_MAX_HZ = 8.0f
    private val FOG_WINDOW_MS = 2000L
    private val FOG_COOLDOWN = 15000L
    
    private var accelDataBuffer = mutableListOf<Pair<Long, Float>>()
    private var lastFogAlertTime = 0L

    private var lastLocation: Location? = null
    private var reminderCheckJob: Job? = null

    companion object {
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_LOCATION_UPDATE = "com.example.careconnect.LOCATION_UPDATE"
        const val ACTION_SEND_SOS = "ACTION_SEND_SOS"
        const val EXTRA_ADDRESS = "extra_address"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRepository = LocationRepository(this)
        reminderRepository = ReminderRepository(this)
        sessionManager = SessionManager(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        createNotificationChannel()
        setupSensors()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    lastLocation = location
                    processNewLocation(location)
                }
            }
        }
        
        startReminderCheck()
    }

    private fun setupSensors() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun startReminderCheck() {
        reminderCheckJob = serviceScope.launch {
            while (isActive) {
                checkReminders()
                delay(60000)
            }
        }
    }

    private suspend fun checkReminders() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return
        
        val now = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
        
        val reminders = reminderRepository.getRemindersForUser(userId)
        for (reminder in reminders) {
            if (reminder.time == currentTime && reminder.lastTriggerDate != todayDate) {
                triggerMedicationAlarm(reminder)
            }
        }
    }

    private fun triggerMedicationAlarm(reminder: Reminder) {
        val intent = Intent(this, MedicationAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_id", reminder.id)
            putExtra("title", reminder.title)
            putExtra("subtitle", reminder.subtitle)
        }
        startActivity(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_SEND_SOS) {
            sendHighPriorityAlert("SOS ALERT", "Manual Emergency help requested!")
            notifyCaretakerViaSms("EMERGENCY SOS: Help needed immediately at my location.")
        }

        val notification = createMonitoringNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(101, notification)
        }
        
        requestLocationUpdates()
        return START_STICKY
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000)
            .setMinUpdateIntervalMillis(15000)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("CareConnect", "Permission lost", e)
        }
    }

    private fun processNewLocation(location: Location) {
        serviceScope.launch {
            val address = getAddress(location.latitude, location.longitude)
            evaluateSafety(location, address)
            
            val intent = Intent(ACTION_LOCATION_UPDATE).apply {
                putExtra(EXTRA_ADDRESS, address)
                putExtra(EXTRA_LATITUDE, location.latitude)
                putExtra(EXTRA_LONGITUDE, location.longitude)
            }
            sendBroadcast(intent)
        }
    }

    private fun evaluateSafety(location: Location, address: String) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        val safeZones = locationRepository.getSafeZones(userId)
        var isSafe = true
        
        if (safeZones.isNotEmpty()) {
            isSafe = false
            for (zone in safeZones) {
                val distance = calculateDistance(location.latitude, location.longitude, zone.lat, zone.lng)
                if (distance <= zone.radius) {
                    isSafe = true
                    break
                }
            }
        }

        locationRepository.logLocation(userId, location.latitude, location.longitude, address, !isSafe)

        if (!isSafe) {
            sendHighPriorityAlert("SAFE ZONE BREACH", "Patient exited the safe boundary.")
            notifyCaretakerViaSms("ALERT: Safe zone breach! Current address: $address")
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 
        val phi1 = lat1 * Math.PI / 180
        val phi2 = lat2 * Math.PI / 180
        val deltaPhi = (lat2 - lat1) * Math.PI / 180
        val deltaLambda = (lon2 - lon1) * Math.PI / 180
        val a = sin(deltaPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val currentTime = System.currentTimeMillis()

            handleFallLogic(magnitude, currentTime)
            handleFogLogic(magnitude, currentTime)
        }
    }

    private fun handleFallLogic(mag: Float, time: Long) {
        // 1. Detect Free Fall (Weightlessness)
        if (mag < FREE_FALL_THRESHOLD) {
            if (freeFallStartTime == 0L) freeFallStartTime = time
        } else {
            if (freeFallStartTime != 0L && (time - freeFallStartTime) >= FREE_FALL_DURATION) {
                // Free fall ended, waiting for impact
                lastImpactTime = time
            }
            freeFallStartTime = 0L
        }

        // 2. Detect Impact Spike
        if (mag > FALL_THRESHOLD) {
            val sinceImpact = time - lastImpactTime
            if (sinceImpact < IMPACT_WINDOW && (time - lastFallAlertTime) > FALL_COOLDOWN) {
                // Potential impact after free fall
                isMonitoringStillness = true
                stillnessStartTime = time
            }
        }

        // 3. Monitor Stillness after Impact
        if (isMonitoringStillness) {
            if (mag in STILL_MIN..STILL_MAX) {
                if ((time - stillnessStartTime) >= STILL_DURATION) {
                    triggerFallAlert()
                    isMonitoringStillness = false
                }
            } else {
                // Movement detected, cancel stillness check
                isMonitoringStillness = false
            }
        }
    }

    private fun handleFogLogic(mag: Float, time: Long) {
        accelDataBuffer.add(time to mag)
        // Keep window of 2 seconds
        while (accelDataBuffer.isNotEmpty() && (time - accelDataBuffer.first().first) > FOG_WINDOW_MS) {
            accelDataBuffer.removeAt(0)
        }

        if (accelDataBuffer.size > 20 && (time - lastFogAlertTime) > FOG_COOLDOWN) {
            val zeroCrossings = countZeroCrossings(accelDataBuffer)
            val frequency = zeroCrossings / (FOG_WINDOW_MS / 1000.0f)
            
            // Check if speed is low and frequency is in tremor range
            if (frequency in FOG_MIN_HZ..FOG_MAX_HZ && (lastLocation?.speed ?: 0f) < 0.2f) {
                triggerFogAlert()
                lastFogAlertTime = time
            }
        }
    }

    private fun countZeroCrossings(data: List<Pair<Long, Float>>): Int {
        val avg = data.map { it.second }.average().toFloat()
        var crossings = 0
        for (i in 1 until data.size) {
            if ((data[i-1].second - avg) * (data[i].second - avg) < 0) {
                crossings++
            }
        }
        return crossings
    }

    private fun triggerFallAlert() {
        lastFallAlertTime = System.currentTimeMillis()
        val intent = Intent(this, FallAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("lat", lastLocation?.latitude)
            putExtra("lng", lastLocation?.longitude)
        }
        startActivity(intent)
        
        sendHighPriorityAlert("FALL DETECTED", "A critical fall has been detected!")
        notifyCaretakerViaSms("URGENT: Fall detected at ${lastLocation?.latitude}, ${lastLocation?.longitude}")
    }

    private fun triggerFogAlert() {
        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 1000)
        
        val userId = sessionManager.getUserId()
        locationRepository.logFogIncident(userId, 5, lastLocation?.latitude ?: 0.0, lastLocation?.longitude ?: 0.0)
        
        sendHighPriorityAlert("FOG INCIDENT", "Freezing of gait detected. Metronome started.")
        notifyCaretakerViaSms("ALERT: Freezing of Gait (FOG) detected for patient.")
    }

    private fun notifyCaretakerViaSms(message: String) {
        val sharedPrefs = getSharedPreferences("CareConnectPrefs", Context.MODE_PRIVATE)
        val sosNumber = sharedPrefs.getString("sos_number", "")
        if (!sosNumber.isNullOrEmpty()) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(sosNumber, null, message, null, null)
            } catch (e: Exception) { Log.e("CareConnect", "SMS failed: ${e.message}") }
        }
    }

    private fun sendHighPriorityAlert(title: String, message: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_alerts)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getAddress(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) addresses[0].getAddressLine(0) else "Unknown"
        } catch (e: Exception) { "Unknown" }
    }

    private fun createMonitoringNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("CareConnect Active")
            .setContentText("Monitoring safety and health in background")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Safety Monitor", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        sensorManager.unregisterListener(this)
    }
}
