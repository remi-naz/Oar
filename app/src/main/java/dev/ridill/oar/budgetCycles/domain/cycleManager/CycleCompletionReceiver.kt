package dev.ridill.oar.budgetCycles.domain.cycleManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleError
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.logW
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
        if (context == null || intent?.action != CycleManager.completionAction(context)) return
        val id = intent.getLongExtra(CycleManager.EXTRA_CYCLE_ID, -1L)
            .takeIf { it > -1L }
            ?: run {
                logW("CycleReceiver") { "Cycle completion alarm received without a valid cycle ID" }
                return
            }
        logI("CycleReceiver") { "Cycle completion alarm received, cycle = $id" }
        val pendingResult = goAsync()
        applicationContext.launch {
            try {
                completeCycleAndNotify(id, context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun completeCycleAndNotify(id: Long, context: Context) {
        when (val result = repo.completeCycleNowAndStartNext(id)) {
            is Result.Error -> {
                // Cycle was already completed by catch-up or manually; nothing to report
                if (result.error == BudgetCycleError.CYCLE_NOT_ACTIVE) {
                    logD("CycleReceiver") { "Cycle $id already completed, skipping" }
                    return
                }
                logW("CycleReceiver") { "Completing cycle $id failed: ${result.error}" }
                notificationHelper.postCycleStartFailed(context, id)
            }

            is Result.Success -> notificationHelper.postCycleCompleted(context, id, result.data)
        }
    }
}
