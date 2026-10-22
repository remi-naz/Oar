package dev.ridill.oar.budgetCycles.domain.cycleManager

import android.content.Context
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleError
import dev.ridill.oar.core.domain.model.Result
import java.time.LocalDateTime

interface CycleManager {
    companion object {
        fun completionAction(context: Context): String = "${context.packageName}.budgetCycle.CYCLE_COMPLETE"
        const val EXTRA_CYCLE_ID = "EXTRA_CYCLE_ID"
    }

    fun cancelCycleCompletion(cycleId: Long)

    fun scheduleCycleCompletion(
        cycleId: Long,
        endDate: LocalDateTime
    ): Result<Unit, BudgetCycleError>
}