package dev.ridill.oar.moneyPiles.domain.notification

import android.annotation.SuppressLint
import android.app.Notification
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
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior

@SuppressLint("MissingPermission")
class PileReminderNotificationHelper(
    private val context: Context
) : NotificationHelper<MoneyPileDetails> {
    private val notificationManager = NotificationManagerCompat.from(context)

    override val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_PILE_REMINDERS"

    private val summaryId: String
        get() = "${context.packageName}.PILE_REMINDERS_SUMMARY"

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
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.notification_channel_pile_reminders_name))
            .setGroup(NotificationHelper.Groups.others(context))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(buildContentIntent(data = null))
            .setGroup(summaryId)

    override fun postNotification(id: Int, data: MoneyPileDetails) {
        if (!notificationManager.areNotificationsEnabled()) return

        val amountText = TextFormat.currency(
            amount = data.reminderAmount.orZero(),
            currency = data.currency
        )
        val contentText = if (data.reminderBehavior == PileReminderBehavior.AUTO_ADD) {
            context.getString(
                R.string.pile_reminder_amount_auto_added_to_pile,
                amountText,
                data.name
            )
        } else {
            context.getString(
                R.string.pile_reminder_time_to_add_amount_to_pile,
                amountText,
                data.name
            )
        }

        val notification = buildBaseNotification()
            .setContentTitle(context.getString(R.string.pile_reminder_notification_title))
            .setContentText(contentText)
            .setContentIntent(buildContentIntent(data))
            .build()

        val summaryNotification = buildBaseNotification()
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .build()

        with(notificationManager) {
            notify(id, notification)
            notify(summaryId.hashCode(), summaryNotification)
        }
    }

    override fun updateNotification(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)
    }

    override fun dismissNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildContentIntent(data: MoneyPileDetails?): PendingIntent {
        val intent = OarDeepLink.moneyPileDetailsIntent(context, data?.id)
        return PendingIntent.getActivity(
            context,
            CONTENT_INTENT_REQUEST_CODE.hashCode(),
            intent,
            UtilConstants.pendingIntentFlags
        )
    }
}

private const val CONTENT_INTENT_REQUEST_CODE = "PILE_REMINDER_CONTENT_INTENT"
