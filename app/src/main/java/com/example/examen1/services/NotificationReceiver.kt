package com.example.examen1.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.examen1.R
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {
    private val TAG = "NotificationReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive iniciado")
        try {
            val allergenName = intent.getStringExtra("allergenName")
            val daysRemaining = intent.getIntExtra("daysRemaining", 0)

            Log.d(TAG, "Datos recibidos: allergenName=$allergenName, daysRemaining=$daysRemaining")

            showTestNotification(context, allergenName ?: "Test", daysRemaining)
        } catch (e: Exception) {
            Log.e(TAG, "Error en onReceive", e)
        }
    }

    private fun showTestNotification(context: Context, allergenName: String, daysRemaining: Int) {
        Log.d(TAG, "Iniciando showTestNotification")

        val channelId = "test_channel"
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        // Crear canal para Android 8.0 (API 26) y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creando canal de notificación")
            val channel = NotificationChannel(
                channelId,
                "Test Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para pruebas de notificaciones"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Test - Control de Alérgeno")
            .setContentText("Test para $allergenName: $daysRemaining días")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        // Mostrar la notificación
        val notificationId = allergenName.hashCode()
        Log.d(TAG, "Mostrando notificación con ID: $notificationId")
        notificationManager.notify(notificationId, notification)
    }
}