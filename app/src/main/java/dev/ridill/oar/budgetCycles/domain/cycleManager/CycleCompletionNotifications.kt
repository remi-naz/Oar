package dev.ridill.oar.budgetCycles.domain.cycleManager

import android.content.Context
import androidx.core.app.NotificationCompat
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleSummary
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.ui.util.TextFormat

fun NotificationHelper<BudgetCycleEntry>.postCycleCompleted(
    context: Context,
    cycleId: Long,
    summary: BudgetCycleSummary
) = postNotification(cycleId.hashCode()) {
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

fun NotificationHelper<BudgetCycleEntry>.postCycleStartFailed(
    context: Context,
    cycleId: Long
) = postNotification(cycleId.hashCode()) {
    this.setContentTitle(context.getString(R.string.error_failed_to_start_cycle))
}
