package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.SilentBeaconApp

class SilenceAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == EmergencyAlarmManager.ACTION_SILENCE_ALARM) {
            val app = context.applicationContext as? SilentBeaconApp
            app?.alarmManager?.silenceAlarm()
        }
    }
}
