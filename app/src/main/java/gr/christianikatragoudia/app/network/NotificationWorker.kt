package gr.christianikatragoudia.app.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import gr.christianikatragoudia.app.data.NotificationRepo
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.TheDatabase

class NotificationWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (TheDatabase.getInstance(context).songDao().count() == 0)
            return Result.failure()
        if (!NotificationRepo(context).areNotificationsEnabled())
            return Result.failure()
        return try {
            val oldTimestamp = SettingsRepo(context).getNotificationTimestamp()
                ?: SettingsRepo(context).getUpdateTimestamp()
            val newTimestamp = WebApp.retrofitService.getNotificationTimestamp()
            if (oldTimestamp == null || oldTimestamp < newTimestamp) {
                SettingsRepo(context).setNotificationTimestamp(newTimestamp)
                NotificationRepo(context).createNewContentNotification()
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
