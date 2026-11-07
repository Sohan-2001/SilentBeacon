package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.SilentBeaconApp

class MeshRelayService : Service() {

    companion object {
        const val CHANNEL_ID = "SilentBeaconMeshChannel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_RELAY = "com.example.service.START_RELAY"
        const val ACTION_STOP_RELAY = "com.example.service.STOP_RELAY"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, MeshRelayService::class.java).apply {
                    action = ACTION_START_RELAY
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("MeshRelayService", "Unable to start MeshRelayService (likely Android Go background restriction)", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, MeshRelayService::class.java).apply {
                    action = ACTION_STOP_RELAY
                }
                context.stopService(intent)
            } catch (e: Exception) {
                android.util.Log.e("MeshRelayService", "Unable to stop MeshRelayService", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_RELAY) {
            try {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (e: Exception) {
                // Ignore
            }
            return START_NOT_STICKY
        }

        val notification = buildForegroundNotification()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    )
                } catch (se: SecurityException) {
                    // Fallback for Android Go or devices restricting foreground service types
                    startForeground(NOTIFICATION_ID, notification)
                } catch (iae: IllegalArgumentException) {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("MeshRelayService", "startForeground failed safely", e)
        }

        // Ensure mesh is active if initialized
        val app = application as? SilentBeaconApp
        try {
            app?.meshManager?.startMesh()
        } catch (e: Exception) {
            android.util.Log.e("MeshRelayService", "Failed starting mesh from service", e)
        }

        return START_STICKY
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SilentBeacon Mesh Active")
            .setContentText("Relaying emergency signals via local radio mesh...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency Mesh Relay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background peer-to-peer radio relay for emergency coordination"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
