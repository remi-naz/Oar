package dev.ridill.oar.statistics.domain.repository

import dev.ridill.oar.statistics.domain.model.CycleBarEntry
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.StatisticsCycleSummary
import dev.ridill.oar.statistics.domain.model.TagSpendEntry
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getCycleSummary(cycleId: Long, addExcluded: Boolean): Flow<StatisticsCycleSummary?>
    fun getRecentCycleBars(
        cycleId: Long,
        limit: Int = RECENT_CYCLES_LIMIT,
        addExcluded: Boolean
    ): Flow<List<CycleBarEntry>>

    fun getTagBreakdown(cycleId: Long, addExcluded: Boolean): Flow<List<TagSpendEntry>>
    fun getLargestSpend(cycleId: Long, addExcluded: Boolean): Flow<LargestSpend?>

    companion object {
        const val RECENT_CYCLES_LIMIT = 6
    }
}
