package dev.ridill.oar.settings.domain.appInit

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.cycleManager.postCycleCompleted
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.logI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "BudgetCycleInitWorker"

@HiltWorker
class BudgetCycleInitWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repo: BudgetCycleRepository,
    private val notificationHelper: NotificationHelper<BudgetCycleEntry>
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForeground()
        logI(TAG) { "$TAG started" }
        // Close an overdue cycle first so the user still gets the completion summary
        val overdueCycleId = repo.getActiveCycle()?.id
        val overdueResult = repo.completeOverdueCycleIfAny()
        if (overdueResult is dev.ridill.oar.core.domain.model.Result.Success) {
            val summary = overdueResult.data
            if (summary != null && overdueCycleId != null)
                notificationHelper.postCycleCompleted(appContext, overdueCycleId, summary)
        }
        val result = repo.scheduleLastCycleOrNew()
        return@withContext when (result) {
            is dev.ridill.oar.core.domain.model.Result.Error -> {
                Result.retry()
            }

            is dev.ridill.oar.core.domain.model.Result.Success -> {
                Result.success()
            }
        }
    }

    private suspend fun startForeground() {
        val notification = notificationHelper.buildBaseNotification()
            .setContentTitle(appContext.getString(R.string.starting_cycle))
            .setProgress(100, 0, true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setForeground(
                ForegroundInfo(
                    AppInitWorkManager.RESTORE_WORKER_NOTIFICATION_ID.hashCode(),
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            )
        } else {
            setForeground(
                ForegroundInfo(
                    AppInitWorkManager.APP_INIT_NOTIFICATION_ID.hashCode(),
                    notification
                )
            )
        }
    }
}