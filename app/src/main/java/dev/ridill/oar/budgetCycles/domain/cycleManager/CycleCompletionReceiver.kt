package dev.ridill.oar.budgetCycles.domain.cycleManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CycleCompletionReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationContext: CoroutineScope

    @Inject
    lateinit var repo: BudgetCycleRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<BudgetCycleEntry>

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != CycleManager.action(context)) return
        val id = intent.getLongExtra(CycleManager.EXTRA_CYCLE_ID, -1L)
            .takeIf { it > -1L }
            ?: return
        logD("CycleReceiver") { "CycleCompletionReceiver: $id" }
        val pendingResult = goAsync()
        try {
            completeCycleAndNotify(id, context)
        } finally {
            pendingResult.finish()
        }
    }

    private fun completeCycleAndNotify(id: Long, context: Context) = applicationContext.launch {
        when (val result = repo.completeCycleNowAndStartNext(id)) {
            is Result.Error -> {
                notificationHelper.postNotification(id.hashCode()) {
                    this.setContentTitle(context.getString(R.string.error_failed_to_start_cycle))
                }
            }

            is Result.Success -> {
                val summary = result.data
                notificationHelper.postNotification(id.hashCode()) {
                    this.setContentTitle(context.getString(R.string.cycle_completed))
                    this.setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(context.getString(R.string.cycle_completed))
                            .setSummaryText(
                                context.getString(
                                    R.string.cycle_summary,
                                    context.getString(summary.aggregateType.labelRes),
                                    TextFormat.currency(summary.aggregateAmount, summary.currency)
                                )
                            )
                    )
                }
            }
        }
    }
}