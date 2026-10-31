package dev.ridill.oar.statistics.presentation

import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.core.domain.model.ChartSlice
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.statistics.domain.model.CycleSummary
import dev.ridill.oar.statistics.presentation.components.StatTile
import java.util.Currency

data class StatisticsState(
    val selectedCycle: BudgetCycleEntry? = null,
    val summary: CycleSummary? = null,
    val tagSlices: List<ChartSlice> = emptyList(),
    val statTileRows: List<List<StatTile>> = emptyList(),
    val currency: Currency = LocaleUtil.defaultCurrency,
    val showExcluded: Boolean = false,
    val isEmpty: Boolean = false
)
