package dev.ridill.oar.moneyPiles.domain.pileReminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails

internal class AlarmManagerPileReminder(
    private val context: Context,
    private val receiverService: ReceiverService,
) : PileReminder {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun setReminder(pile: MoneyPileDetails) {
        val timeMillis = pile.nextReminderTimestamp
            ?.let { DateUtil.toMillis(it) } ?: return
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC,
            timeMillis,
            buildPendingIntent(pile.id)
        )
        receiverService.toggleBootAndTimeReceivers(true)

        logI(PileReminder::class.simpleName) { "Set reminder for pile ID ${pile.id} on ${pile.nextReminderTimestamp}" }
    }

    override fun cancel(id: Long) {
        alarmManager.cancel(buildPendingIntent(id))

        logI(PileReminder::class.simpleName) { "Pile ID $id reminder cancelled" }
    }

    private fun buildPendingIntent(id: Long): PendingIntent {
        val intent = Intent(context, PileReminderReceiver::class.java).apply {
            action = PileReminder.ACTION
            putExtra(PileReminder.EXTRA_PILE_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            UtilConstants.pendingIntentFlags
        )
    }
}
