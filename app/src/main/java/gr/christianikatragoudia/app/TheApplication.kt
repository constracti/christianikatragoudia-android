package gr.christianikatragoudia.app

import android.app.Application
import gr.christianikatragoudia.app.data.NotificationRepo
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.TheDatabase
import gr.christianikatragoudia.app.data.WorkManagerRepo

class TheApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationRepo(this).createNewContentChannel()
        WorkManagerRepo(this).enqueueNotificationWorker()
    }
}
