package com.example.proyectfaseii.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.proyectfaseii.R
import com.example.proyectfaseii.ui.activities.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "habitreminder_channel"
    private const val CHANNEL_NAME = "Recordatorio de Hábitos"
    private const val CHANNEL_DESC = "Recibe un recordatorio diario para tus hábitos"
    private const val NOTIFICATION_ID = 1001

    /**
     * Envía una notificación diaria si el permiso está concedido
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendNotification(context: Context, title: String, message: String) {
        // Verificación segura en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = NotificationManagerCompat.from(context).areNotificationsEnabled()
            if (!hasPermission) {
                Toast.makeText(context, "Activa las notificaciones en ajustes", Toast.LENGTH_SHORT).show()
                return
            }
        }

        createNotificationChannel(context)

        val manager = NotificationManagerCompat.from(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // ✅ Se cierra al tocar
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
