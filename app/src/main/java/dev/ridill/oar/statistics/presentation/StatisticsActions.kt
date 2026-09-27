package dev.ridill.oar.statistics.presentation

import dev.ridill.oar.statistics.domain.model.StatisticsChartMode

interface StatisticsActions {
    fun onExcludedToggle()
    fun onChartModeChange(mode: StatisticsChartMode)
    fun onBarSelect(cycleId: Long)
    fun onTagSelect(tagId: Long)
    fun onCycleSelect(cycleId: Long?)
}
