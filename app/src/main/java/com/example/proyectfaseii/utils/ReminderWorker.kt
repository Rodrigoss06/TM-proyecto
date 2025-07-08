package com.example.proyectfaseii.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyectfaseii.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = SharedPrefManager.getInstance(applicationContext)

        return@withContext try {
            if (prefs.areNotificationsEnabled()) {
                NotificationHelper.sendNotification(
                    applicationContext,
                    title = "¡Hora de tu hábito!",
                    message = "Revisa tus hábitos y marca tu progreso diario."
                )
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
