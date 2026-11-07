package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.EmergencyTicket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmergencyAlarmManager(private val context: Context) {

    companion object {
        private const val TAG = "EmergencyAlarmManager"
        const val ALARM_CHANNEL_ID = "critical_emergency_alarm_channel"
        const val NOTIFICATION_ID = 9999
        const val ACTION_SILENCE_ALARM = "com.example.action.SILENCE_ALARM"
        private const val MAX_ALARM_DURATION_MS = 60_000L // Automatically silence after 60s
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _isAlarmActive = MutableStateFlow(false)
    val isAlarmActive: StateFlow<Boolean> = _isAlarmActive.asStateFlow()

    private val _activeCriticalTicket = MutableStateFlow<EmergencyTicket?>(null)
    val activeCriticalTicket: StateFlow<EmergencyTicket?> = _activeCriticalTicket.asStateFlow()

    private var ringtone: Ringtone? = null
    private var autoSilenceJob: Job? = null
    private var vibrator: Vibrator? = null

    // Track already alerted tickets to avoid re-triggering on duplicate gossip relays
    private val alertedTicketIds = mutableSetOf<String>()

    init {
        createNotificationChannel()
        loadAlertedTickets()
        initVibrator()
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Critical Emergency Siren",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority sound and siren for critical life-threatening emergencies"
                setSound(alarmSound, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 200, 600, 200, 800, 300)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadAlertedTickets() {
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("alerted_ticket_ids", emptySet()) ?: emptySet()
        alertedTicketIds.addAll(saved)
    }

    private fun saveAlertedTicket(ticketId: String) {
        alertedTicketIds.add(ticketId)
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("alerted_ticket_ids", alertedTicketIds).apply()
    }

    /**
     * Algorithm for critical alarm:
     * 1. Checks if ticket was already alerted (deduplication).
     * 2. Marks ticket as alerted.
     * 3. Plays urgent alarm ringtone with USAGE_ALARM stream.
     * 4. Starts repeating SOS vibration pulses.
     * 5. Displays high-priority heads-up notification with action to silence or view.
     * 6. Automatically times out after 60 seconds to preserve battery.
     */
    fun triggerEmergencyAlarm(ticket: EmergencyTicket) {
        if (alertedTicketIds.contains(ticket.ticketId)) {
            Log.d(TAG, "Ticket ${ticket.ticketId} was already alarmed on this device. Suppressing duplicate siren.")
            return
        }

        saveAlertedTicket(ticket.ticketId)

        scope.launch {
            _activeCriticalTicket.value = ticket
            _isAlarmActive.value = true

            startAudioAlarm()
            startVibration()
            showAlarmNotification(ticket)

            // Auto silence after timeout
            autoSilenceJob?.cancel()
            autoSilenceJob = launch {
                delay(MAX_ALARM_DURATION_MS)
                if (_isAlarmActive.value) {
                    silenceAlarm()
                }
            }
        }
    }

    private fun startAudioAlarm() {
        try {
            stopAudioAlarm()
            var alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            ringtone = RingtoneManager.getRingtone(context, alertUri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                }
                play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing alarm ringtone", e)
        }
    }

    private fun stopAudioAlarm() {
        try {
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            ringtone = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed stopping alarm ringtone", e)
        }
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 600, 200, 600, 200, 800, 300)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0) // Repeat from index 0
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed starting vibration", e)
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed stopping vibration", e)
        }
    }

    private fun showAlarmNotification(ticket: EmergencyTicket) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_TICKET_ID", ticket.ticketId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val silenceIntent = Intent(context, SilenceAlarmReceiver::class.java).apply {
            action = ACTION_SILENCE_ALARM
        }
        val silencePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            silenceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setContentTitle("🚨 CRITICAL EMERGENCY BROADCAST")
            .setContentText("${ticket.title} • ${ticket.locationDescription}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${ticket.title}\n📍 ${ticket.locationDescription}\n${ticket.description}")
            )
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "SILENCE ALARM",
                silencePendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun silenceAlarm() {
        scope.launch {
            autoSilenceJob?.cancel()
            autoSilenceJob = null
            stopAudioAlarm()
            stopVibration()
            _isAlarmActive.value = false
            _activeCriticalTicket.value = null
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    fun onTicketResolved(ticketId: String) {
        if (_activeCriticalTicket.value?.ticketId == ticketId) {
            silenceAlarm()
        }
    }
}
