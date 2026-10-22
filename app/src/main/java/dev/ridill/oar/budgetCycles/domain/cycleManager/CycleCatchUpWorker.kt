package dev.ridill.oar.budgetCycles.domain.cycleManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleError
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.logI

private const val TAG = "CycleCatchUpWorker"

/**
 * Safety net for a lost cycle-completion alarm (force stop, OEM battery management, etc).
 * Completes the active cycle if it is overdue, otherwise re-schedules its alarm.
 */
@HiltWorker
class CycleCatchUpWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repo: BudgetCycleRepository,
    private val notificationHelper: NotificationHelper<BudgetCycleEntry>
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        logI(TAG) { "$TAG started" }
        val overdueCycleId = repo.getActiveCycle()?.id
        return when (val result = repo.completeOverdueCycleIfAny()) {
            is dev.ridill.oar.core.domain.model.Result.Success -> {
                val summary = result.data
                if (summary != null && overdueCycleId != null) {
                    notificationHelper.postCycleCompleted(appContext, overdueCycleId, summary)
                }
                Result.success()
            }

            is dev.ridill.oar.core.domain.model.Result.Error -> when (result.error) {
                // Completed by the alarm or manually in the meantime
                BudgetCycleError.CYCLE_NOT_ACTIVE -> Result.success()
                else -> Result.retry()
            }
        }
    }
}
