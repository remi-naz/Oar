package dev.ridill.oar.core.domain.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

interface NotificationHelper<T> {
    val channelId: String
    fun registerChannelGroup()
    fun registerChannel()
    fun buildBaseNotification(): NotificationCompat.Builder
    fun postNotification(id: Int, data: T) {}
    fun postNotification(
        id: Int,
        builder: NotificationCompat.Builder.() -> NotificationCompat.Builder
    ) {}

    fun updateNotification(id: Int, notification: Notification) {}
    fun dismissNotification(id: Int) {}
    fun dismissAllNotifications() {}

    object Groups {
        fun schedules(context: Context): String =
            "${context.packageName}.NOTIFICATION_CHANNEL_GROUP_NOTIFICATION"

        fun others(context: Context): String =
            "${context.packageName}.NOTIFICATION_CHANNEL_GROUP_OTHERS"
    }

    object Utils {
        const val TIMEOUT_MILLIS = 5_000L
    }
}