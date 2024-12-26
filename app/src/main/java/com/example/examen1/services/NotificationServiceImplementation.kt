package com.example.examen1.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.examen1.R
import java.util.concurrent.TimeUnit

class AllergenNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val controlId = inputData.getString("controlId") ?: return Result.failure()
        val allergenName = inputData.getString("allergenName") ?: return Result.failure()
        val daysRemaining = inputData.getInt("daysRemaining", 0)

        showNotification(allergenName, daysRemaining)
        return Result.success()
    }

    private fun showNotification(allergenName: String, daysRemaining: Int) {
        val channelId = "allergen_control_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0 y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Control de Alérgenos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de control de alérgenos"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Control de Alérgeno Activo")
            .setContentText("Control de $allergenName: $daysRemaining días restantes")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun scheduleNotification(
            context: Context,
            controlId: String,
            allergenName: String,
            daysRemaining: Int,
            notifyTime: String
        ) {
            val timeComponents = notifyTime.split(":")
            val hour = timeComponents[0].toInt()
            val minute = timeComponents[1].toInt()

            val currentDate = java.util.Calendar.getInstance()
            val dueDate = java.util.Calendar.getInstance()

            // Configurar hora de notificación
            dueDate.set(java.util.Calendar.HOUR_OF_DAY, hour)
            dueDate.set(java.util.Calendar.MINUTE, minute)
            dueDate.set(java.util.Calendar.SECOND, 0)

            // Si la hora ya pasó, programar para mañana
            if (dueDate.before(currentDate)) {
                dueDate.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }

            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            val inputData = workDataOf(
                "controlId" to controlId,
                "allergenName" to allergenName,
                "daysRemaining" to daysRemaining
            )

            val notificationWork = OneTimeWorkRequestBuilder<AllergenNotificationWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "allergen_notification_$controlId",
                    ExistingWorkPolicy.REPLACE,
                    notificationWork
                )
        }

        fun cancelNotification(context: Context, controlId: String) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("allergen_notification_$controlId")
        }
    }
}