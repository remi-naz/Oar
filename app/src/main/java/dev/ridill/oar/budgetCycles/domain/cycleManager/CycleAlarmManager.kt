package dev.ridill.oar.budgetCycles.domain.cycleManager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.ui.util.UiText
import java.time.LocalDateTime

class CycleAlarmManager(
    private val context: Context,
    private val receiverService: ReceiverService
) : CycleManager {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun cancelCycleCompletion(cycleId: Long) {
        val pendingIntent = buildPendingIntent(cycleId)
        alarmManager.cancel(pendingIntent)
    }

    override fun scheduleCycleCompletion(
        cycleId: Long,
        endDate: LocalDateTime
    ): Result<Unit, BudgetCycleError> = try {
        val endTimeMillis = DateUtil.toMillis(endDate)
        val pendingIntent = buildPendingIntent(cycleId)
        // Inexact on purpose: rollover doesn't need minute precision, so no exact alarm permission
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            endTimeMillis,
            pendingIntent
        )
        // Re-register the alarm after reboot / time changes even if no schedules or piles exist
        receiverService.toggleBootAndTimeReceivers(true)

        logD(CycleManager::class.simpleName) { "Scheduled cycle ID = $cycleId to complete at $endDate" }
        Result.Success(Unit)
    } catch (t: Throwable) {
        logE(t, CycleManager::class.simpleName)
        Result.Error(
            BudgetCycleError.CYCLE_SCHEDULE_FAILED,
            UiText.StringResource(R.string.error_failed_to_schedule_cycle, true)
        )
    }

    private fun buildPendingIntent(cycleId: Long): PendingIntent {
        val intent = Intent(context, CycleCompletionReceiver::class.java).apply {
            this.action = CycleManager.completionAction(context)
            putExtra(CycleManager.EXTRA_CYCLE_ID, cycleId)
        }
        return PendingIntent.getBroadcast(
            context,
            cycleId.hashCode(),
            intent,
            UtilConstants.pendingIntentFlags
        )
    }
}