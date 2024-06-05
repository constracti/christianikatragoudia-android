package gr.christianikatragoudia.app.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import gr.christianikatragoudia.app.network.NotificationWorker
import java.time.Duration

class WorkManagerRepo(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun enqueueNotificationWorker() {
        val workerClass = NotificationWorker::class.java
        val repeatInterval = Duration.ofHours(1L)
        val workRequest = PeriodicWorkRequest.Builder(workerClass, repeatInterval).build()
        val workName = "notification_worker"
        workManager.enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }
}
