package dev.ridill.oar.statistics.presentation

import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.statistics.domain.model.CycleBarEntry
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.StatisticsChartMode
import dev.ridill.oar.statistics.domain.model.CycleSummary
import dev.ridill.oar.statistics.domain.model.TagSpendEntry

data class StatisticsState(
    val selectedCycle: BudgetCycleEntry? = null,
    val summary: CycleSummary? = null,
    val cycleElapsedDays: Int = Int.Zero,
    val cycleTotalDays: Int = Int.Zero,
    val isCycleOnPace: Boolean = false,
    val cycleBars: List<CycleBarEntry> = emptyList(),
    val selectedCycleBar: CycleBarEntry? = null,
    val chartMode: StatisticsChartMode = StatisticsChartMode.SPEND,
    val tagBreakdown: List<TagSpendEntry> = emptyList(),
    val selectedTagEntry: TagSpendEntry? = null,
    val largestSpend: LargestSpend? = null,
    val showExcluded: Boolean = false,
    val isEmpty: Boolean = false
)
