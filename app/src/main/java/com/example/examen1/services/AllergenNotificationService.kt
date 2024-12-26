package com.example.examen1.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.examen1.R
import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale

class AllergenNotificationService : Service() {
    private val TAG = "AllergenService"
    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_SCHEDULE_NOTIFICATION -> {
                val allergenName = intent.getStringExtra(EXTRA_ALLERGEN_NAME)
                val daysRemaining = intent.getIntExtra(EXTRA_DAYS_REMAINING, 0)
                val notificationTime = intent.getStringExtra(EXTRA_NOTIFICATION_TIME)

                if (allergenName != null && notificationTime != null && daysRemaining > 0) {
                    scheduleNotification(allergenName, daysRemaining, notificationTime)
                }
            }
            ACTION_SHOW_NOTIFICATION -> {
                val allergenName = intent.getStringExtra(EXTRA_ALLERGEN_NAME)
                val daysRemaining = intent.getIntExtra(EXTRA_DAYS_REMAINING, 0)
                if (allergenName != null && daysRemaining > 0) {
                    showNotification(allergenName, daysRemaining)
                    // Programar la siguiente notificación para mañana
                    val notificationTime = getPreviousNotificationTime(allergenName)
                    if (notificationTime != null) {
                        scheduleNotification(allergenName, daysRemaining - 1, notificationTime)
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun getPreviousNotificationTime(allergenName: String): String? {
        val prefs = getSharedPreferences("allergen_notifications", Context.MODE_PRIVATE)
        return prefs.getString("time_$allergenName", null)
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Allergen Controls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for allergen controls"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotification(allergenName: String, daysRemaining: Int, notificationTime: String) {
        if (daysRemaining <= 0) {
            Log.d(TAG, "No scheduling notification - days remaining is 0 or negative")
            return
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        try {
            val (hours, minutes) = notificationTime.split(":").map { it.toInt() }
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            // Guardar el tiempo de notificación para uso futuro
            getSharedPreferences("allergen_notifications", Context.MODE_PRIVATE)
                .edit()
                .putString("time_$allergenName", notificationTime)
                .apply()

            // Intent para el AlarmReceiver
            val intent = Intent(this, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ALARM_ACTION
                putExtra(EXTRA_ALLERGEN_NAME, allergenName)
                putExtra(EXTRA_DAYS_REMAINING, daysRemaining)
            }

            val requestCode = (allergenName + calendar.timeInMillis.toString()).hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            }

            showConfirmationNotification(notificationTime)

        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling notification", e)
        }
    }

    private fun showConfirmationNotification(time: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Notificación Programada")
            .setContentText("Se programó la notificación para las $time")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(CONFIRMATION_NOTIFICATION_ID, notification)
    }

    private fun showNotification(allergenName: String, daysRemaining: Int) {
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Control de Alérgeno Activo")
                .setContentText("Control de $allergenName: $daysRemaining días restantes")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 500, 1000))
                .build()

            val notificationId = allergenName.hashCode()
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "allergen_control_channel"
        const val ACTION_SCHEDULE_NOTIFICATION = "com.example.examen1.action.SCHEDULE_NOTIFICATION"
        const val ACTION_SHOW_NOTIFICATION = "com.example.examen1.action.SHOW_NOTIFICATION"
        private const val EXTRA_ALLERGEN_NAME = "allergenName"
        private const val EXTRA_DAYS_REMAINING = "daysRemaining"
        private const val EXTRA_NOTIFICATION_TIME = "notification_time"
        private const val CONFIRMATION_NOTIFICATION_ID = 9999

        fun scheduleNotification(context: Context, allergenName: String, daysRemaining: Int, notificationTime: String) {
            if (daysRemaining <= 0) return

            Intent(context, AllergenNotificationService::class.java).apply {
                action = ACTION_SCHEDULE_NOTIFICATION
                putExtra(EXTRA_ALLERGEN_NAME, allergenName)
                putExtra(EXTRA_DAYS_REMAINING, daysRemaining)
                putExtra(EXTRA_NOTIFICATION_TIME, notificationTime)
                context.startService(this)
            }
        }
    }
}