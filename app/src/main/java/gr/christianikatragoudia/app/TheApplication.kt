package gr.christianikatragoudia.app

import android.app.Application
import gr.christianikatragoudia.app.data.WorkManagerRepo

class TheApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WorkManagerRepo(this).enqueueNotificationWorker()
    }
}
