package gr.christianikatragoudia.app.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import gr.christianikatragoudia.app.R
import java.time.LocalTime

class NotificationRepo(private val context: Context) {

    private companion object {
        const val NEW_CONTENT_CHANNEL_ID = "channel_new_content"
        const val NEW_CONTENT_NOTIFICATION_ID = 1
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun areNotificationsEnabled(): Boolean = notificationManager.areNotificationsEnabled()

    fun createNewContentChannel() {
        val notificationChannel = NotificationChannel(
            NEW_CONTENT_CHANNEL_ID,
            context.getString(R.string.notification_new_content_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun createNewContentNotification() {
        val notificationBuilder = Notification.Builder(context, NEW_CONTENT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground) // TODO enlarge small icon
            .setContentTitle(context.getString(R.string.notification_new_content_title))
            .setContentText(context.getString(R.string.notification_new_content_text))
        // TODO setContentIntent
        notificationManager.notify(NEW_CONTENT_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun cancelNewContentNotification() {
        notificationManager.cancel(NEW_CONTENT_NOTIFICATION_ID)
    }
}
