package gr.christianikatragoudia.app.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import gr.christianikatragoudia.app.data.SettingsRepo

class NotificationWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (SettingsRepo(context).hasUpdateCheck())
            return Result.success()
        return try {
            val oldTimestamp = SettingsRepo(context).getUpdateTimestamp()
            val newTimestamp = WebApp.retrofitService.getNotificationTimestamp()
            if (oldTimestamp == null || oldTimestamp < newTimestamp) {
                SettingsRepo(context).setUpdateCheck(true)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
