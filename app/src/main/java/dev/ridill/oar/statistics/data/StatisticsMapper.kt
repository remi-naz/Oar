package dev.ridill.oar.statistics.data

import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.statistics.data.local.relation.CycleAggregateRelation
import dev.ridill.oar.statistics.data.local.relation.CycleTotalsRelation
import dev.ridill.oar.statistics.data.local.relation.LargestSpendRelation
import dev.ridill.oar.statistics.data.local.relation.TagSpendRelation
import dev.ridill.oar.statistics.domain.model.CycleBarEntry
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.StatisticsCycleSummary
import dev.ridill.oar.statistics.domain.model.TagSpendEntry
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Currency

fun CycleTotalsRelation.toStatisticsCycleSummary(cycle: BudgetCycleEntry): StatisticsCycleSummary {
    val (daysElapsed, daysTotal) = elapsedAndTotalDays(cycle.startDate, cycle.endDate)
    return StatisticsCycleSummary(
        cycleId = cycle.id,
        startDate = cycle.startDate,
        endDate = cycle.endDate,
        spent = spent,
        received = received,
        budget = cycle.budget,
        currency = cycle.currency,
        transactionCount = transactionCount,
        daysElapsed = daysElapsed,
        daysTotal = daysTotal
    )
}

fun CycleAggregateRelation.toCycleBarEntry(): CycleBarEntry = CycleBarEntry(
    cycleId = id,
    startDate = startDate,
    endDate = endDate,
    spent = spent,
    received = received,
    budget = budget
)

fun TagSpendRelation.toTagSpendEntry(totalSpend: Double): TagSpendEntry = TagSpendEntry(
    tagId = tagId,
    name = tagName,
    colorCode = tagColorCode,
    amount = amount,
    transactionCount = transactionCount,
    fraction = if (totalSpend > 0) (amount / totalSpend).toFloat() else 0f
)

fun LargestSpendRelation.toLargestSpend(currency: Currency): LargestSpend = LargestSpend(
    amount = amount,
    note = note,
    currency = currency
)

private fun elapsedAndTotalDays(startDate: LocalDate, endDate: LocalDate): Pair<Int, Int> {
    val daysTotal = (ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1).coerceAtLeast(1)
    val today = DateUtil.dateNow()
    val daysElapsed = when {
        today.isBefore(startDate) -> 0
        today.isAfter(endDate) -> daysTotal
        else -> ChronoUnit.DAYS.between(startDate, today).toInt() + 1
    }
    return daysElapsed to daysTotal
}
