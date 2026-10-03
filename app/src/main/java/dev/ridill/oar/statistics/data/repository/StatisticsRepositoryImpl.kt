package dev.ridill.oar.statistics.data.repository

import dev.ridill.oar.aggregations.data.local.AggregationsDao
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.statistics.data.local.StatisticsDao
import dev.ridill.oar.statistics.data.local.relation.CycleAggregateRelation
import dev.ridill.oar.statistics.data.toCycleBarEntry
import dev.ridill.oar.statistics.data.toLargestSpend
import dev.ridill.oar.statistics.data.toStatisticsCycleSummary
import dev.ridill.oar.statistics.data.toTagSpendEntry
import dev.ridill.oar.statistics.domain.model.CycleBarEntry
import dev.ridill.oar.statistics.domain.model.CycleSummary
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.TagSpendEntry
import dev.ridill.oar.statistics.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest

internal class StatisticsRepositoryImpl(
    private val dao: StatisticsDao,
    private val aggDao: AggregationsDao,
    private val cycleRepo: BudgetCycleRepository
) : StatisticsRepository {

    override fun getCycleSummary(
        cycleId: Long,
        addExcluded: Boolean
    ): Flow<CycleSummary?> = cycleRepo.getCycleByIdFlow(cycleId)
        .flatMapLatest { cycle ->
            if (cycle == null) flowOf(null)
            else aggDao.getCycleTotals(
                cycleId = cycleId,
                currencyCode = cycle.currency.currencyCode,
                addExcluded = addExcluded,
            ).mapLatest { totals -> totals.toStatisticsCycleSummary(cycle) }
        }.distinctUntilChanged()

    override fun getRecentCycleBars(
        cycleId: Long,
        addExcluded: Boolean,
        limit: Int
    ): Flow<List<CycleBarEntry>> = cycleRepo.getCycleByIdFlow(cycleId)
        .flatMapLatest { cycle ->
            if (cycle == null) flowOf(emptyList())
            else aggDao.getCycleAggregatesGroupedByCycle(
                currencyCode = cycle.currency.currencyCode,
                limit = limit,
                addExcluded = addExcluded
            ).mapLatest { list -> list.map(CycleAggregateRelation::toCycleBarEntry) }
        }.distinctUntilChanged()

    override fun getTagBreakdown(
        cycleId: Long,
        addExcluded: Boolean
    ): Flow<List<TagSpendEntry>> = cycleRepo.getCycleByIdFlow(cycleId)
        .flatMapLatest { cycle ->
            if (cycle == null) flowOf(emptyList())
            else dao.getTagBreakdownForCycle(
                cycleId = cycleId,
                currencyCode = cycle.currency.currencyCode,
                addExcluded = addExcluded
            ).mapLatest { list ->
                val totalSpend = list.sumOf { it.amount }
                list.map { it.toTagSpendEntry(totalSpend) }
            }
        }.distinctUntilChanged()

    override fun getLargestSpend(
        cycleId: Long,
        addExcluded: Boolean
    ): Flow<LargestSpend?> = cycleRepo.getCycleByIdFlow(cycleId)
        .flatMapLatest { cycle ->
            if (cycle == null) flowOf(null)
            else dao.getLargestSpendForCycle(
                cycleId = cycleId,
                currencyCode = cycle.currency.currencyCode,
                addExcluded = addExcluded
            ).mapLatest { relation -> relation?.toLargestSpend(cycle.currency) }
        }.distinctUntilChanged()
}
