package dev.ridill.oar.statistics.data

import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.statistics.data.local.relation.CycleAggregateRelation
import dev.ridill.oar.statistics.data.local.relation.CycleTotalsRelation
import dev.ridill.oar.statistics.data.local.relation.LargestSpendRelation
import dev.ridill.oar.statistics.data.local.relation.TagSpendRelation
import dev.ridill.oar.statistics.domain.model.CycleBarEntry
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.CycleSummary
import dev.ridill.oar.statistics.domain.model.TagSpendEntry
import java.util.Currency

fun CycleTotalsRelation.toStatisticsCycleSummary(cycle: BudgetCycleEntry): CycleSummary {
    return CycleSummary(
        cycleId = cycle.id,
        startDate = cycle.startDate,
        endDate = cycle.endDate,
        spent = spent,
        received = received,
        budget = cycle.budget,
        currency = cycle.currency,
        transactionCount = transactionCount,
    )
}

fun CycleAggregateRelation.toCycleBarEntry(): CycleBarEntry = CycleBarEntry(
    cycleId = id,
    startDate = startDate,
    endDate = endDate,
    spent = spent,
    received = received,
    budget = budget,
    currency = LocaleUtil.currencyForCode(currencyCode),
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
