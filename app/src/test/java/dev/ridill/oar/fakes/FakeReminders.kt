package dev.ridill.oar.fakes

import dev.ridill.oar.budgetCycles.domain.cycleManager.CycleManager
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.pileReminder.PileReminder
import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.scheduleReminder.ScheduleReminder
import java.time.LocalDateTime

class FakeScheduleReminder : ScheduleReminder {
    val cancelledIds = mutableSetOf<Long>()
    val remindersSet = mutableListOf<Schedule>()

    override fun setReminder(schedule: Schedule) {
        remindersSet += schedule
    }

    override fun cancel(id: Long) {
        cancelledIds += id
    }
}

class FakePileReminder : PileReminder {
    val cancelledIds = mutableSetOf<Long>()
    val remindersSet = mutableListOf<MoneyPileDetails>()

    override fun setReminder(pile: MoneyPileDetails) {
        remindersSet += pile
    }

    override fun cancel(id: Long) {
        cancelledIds += id
    }
}

class FakeCycleManager : CycleManager {
    val cancelledCycleIds = mutableSetOf<Long>()
    val scheduledCompletions = mutableMapOf<Long, LocalDateTime>()

    override fun cancelCycleCompletion(cycleId: Long) {
        cancelledCycleIds += cycleId
    }

    override fun scheduleCycleCompletion(
        cycleId: Long,
        endDate: LocalDateTime
    ): Result<Unit, BudgetCycleError> {
        scheduledCompletions[cycleId] = endDate
        return Result.Success(Unit)
    }
}
