package dev.ridill.oar.moneyPiles.domain.pileReminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.repository.PileReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PileReminderReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationContext: CoroutineScope

    @Inject
    lateinit var repo: PileReminderRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<MoneyPileDetails>

    @Inject
    lateinit var pileReminder: PileReminder

    override fun onReceive(context: Context?, intent: Intent?) {
        logI(PileReminderReceiver::class.simpleName) { "onReceive() called" }
        if (intent?.action != PileReminder.ACTION) return
        val id = intent.getLongExtra(PileReminder.EXTRA_PILE_ID, -1L)
            .takeIf { it > -1L }
            ?: return

        val pendingResult = goAsync()
        applicationContext.launch {
            try {
                handleReminder(id)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleReminder(id: Long) {
        logI(PileReminderReceiver::class.simpleName) { "handleReminder() called with: id = $id" }
        val pile = repo.getPileDetails(id) ?: return
        if (pile.completionTimestamp != null) {
            pileReminder.cancel(id)
            return
        }

        val nextReminder = pile.reminderCadence.nextReminderFrom(DateUtil.now())
        repo.recordReminderFired(pile, nextReminder)

        notificationHelper.postNotification(id = pile.id.hashCode(), data = pile)
        pileReminder.setReminder(pile.copy(nextReminderTimestamp = nextReminder))
    }
}
