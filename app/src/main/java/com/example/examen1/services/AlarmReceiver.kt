package com.example.examen1.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
        const val ALARM_ACTION = "com.example.examen1.ALARM_TRIGGER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ALARM_ACTION) {
            Log.d(TAG, "AlarmReceiver onReceive called with correct action")

            val allergenName = intent.getStringExtra("allergenName")
            val daysRemaining = intent.getIntExtra("daysRemaining", 0)

            Log.d(TAG, "Received alarm for: $allergenName, Days: $daysRemaining")

            // Iniciar el servicio para mostrar la notificación
            val serviceIntent = Intent(context, AllergenNotificationService::class.java).apply {
                action = AllergenNotificationService.ACTION_SHOW_NOTIFICATION
                putExtra("allergenName", allergenName)
                putExtra("daysRemaining", daysRemaining)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.startService(serviceIntent)
        }
    }
}