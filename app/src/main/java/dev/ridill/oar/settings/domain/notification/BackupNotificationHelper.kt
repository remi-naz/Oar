package dev.ridill.oar.settings.domain.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.ridill.oar.R
import dev.ridill.oar.application.OarDeepLink
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.UtilConstants

class BackupNotificationHelper(
    private val context: Context
) : NotificationHelper<String> {
    private val notificationManager = NotificationManagerCompat.from(context)

    override val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_BACKUPS"

    init {
        registerChannelGroup()
        registerChannel()
    }

    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat
            .Builder(NotificationHelper.Groups.others(context))
            .setName(context.getString(R.string.notification_channel_group_others_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.notification_channel_backups_name))
            .setGroup(NotificationHelper.Groups.others(context))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(buildContentIntent())

    @SuppressLint("MissingPermission")
    override fun postNotification(id: Int, data: String) {
        val notification = buildBaseNotification()
            .setContentTitle(context.getString(R.string.error_backup_failed_notification_title))
            .setContentText(data)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(data)
                    .bigText(context.getString(R.string.tap_to_resolve_issues))
            )
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    private fun buildContentIntent(): PendingIntent? {
        val intent = OarDeepLink.backupSettingsIntent(context)
        return PendingIntent.getActivity(
            context,
            CONTENT_INTENT_REQUEST_CODE.hashCode(),
            intent,
            UtilConstants.pendingIntentFlags
        )
    }
}

private const val CONTENT_INTENT_REQUEST_CODE = "BACKUP_CONTENT_INTENT"
