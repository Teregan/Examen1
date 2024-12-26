package com.example.examen1.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class NotificationManagerService(private val context: Context) {
    private val TAG = "NotificationManager"
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNotification(
        allergenName: String,
        daysRemaining: Int,
        notificationTime: String // formato "HH:mm"
    ) {
        try {
            val (hours, minutes) = notificationTime.split(":").map { it.toInt() }

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // Si la hora ya pasó, programar para mañana
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            Log.d(TAG, "Programando notificación para: ${dateFormat.format(calendar.time)}")
            Log.d(TAG, "Alérgeno: $allergenName, Días restantes: $daysRemaining")

            // Crear Intent con acción específica
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.example.examen1.ALLERGEN_NOTIFICATION"
                putExtra("allergenName", allergenName)
                putExtra("daysRemaining", daysRemaining)
                putExtra("notificationTime", calendar.timeInMillis)
            }

            // Crear PendingIntent único para cada notificación
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                calendar.timeInMillis.toInt(), // Usar timestamp como requestCode único
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Programar la alarma
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (alarmManager.canScheduleExactAlarms()) {
                        setExactAlarm(calendar.timeInMillis, pendingIntent)
                    } else {
                        setInexactAlarm(calendar.timeInMillis, pendingIntent)
                    }
                }
                else -> setExactAlarm(calendar.timeInMillis, pendingIntent)
            }

            Log.d(TAG, "Alarma programada exitosamente para ${dateFormat.format(Date(calendar.timeInMillis))}")

            // Programar la siguiente alarma para mañana
            scheduleNextDayAlarm(allergenName, daysRemaining - 1, notificationTime)

        } catch (e: Exception) {
            Log.e(TAG, "Error al programar notificación", e)
        }
    }

    private fun setExactAlarm(timeInMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }

    private fun setInexactAlarm(timeInMillis: Long, pendingIntent: PendingIntent) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }

    private fun scheduleNextDayAlarm(allergenName: String, daysRemaining: Int, notificationTime: String) {
        if (daysRemaining > 0) {
            val (hours, minutes) = notificationTime.split(":").map { it.toInt() }
            val tomorrow = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.example.examen1.ALLERGEN_NOTIFICATION"
                putExtra("allergenName", allergenName)
                putExtra("daysRemaining", daysRemaining)
                putExtra("notificationTime", tomorrow.timeInMillis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                tomorrow.timeInMillis.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    setExactAlarm(tomorrow.timeInMillis, pendingIntent)
                } else {
                    setInexactAlarm(tomorrow.timeInMillis, pendingIntent)
                }
            } else {
                setExactAlarm(tomorrow.timeInMillis, pendingIntent)
            }
        }
    }

    fun cancelNotification(allergenName: String) {
        try {
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.action = "com.example.examen1.ALLERGEN_NOTIFICATION"

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                allergenName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Notificación cancelada para: $allergenName")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cancelar notificación", e)
        }
    }
}