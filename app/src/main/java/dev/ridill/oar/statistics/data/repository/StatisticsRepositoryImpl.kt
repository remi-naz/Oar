package dev.ridill.oar.statistics.data.repository

import com.zhuinden.flowcombinetuplekt.combineTuple
import dev.ridill.oar.aggregations.data.local.AggregationsDao
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.statistics.data.local.relation.CycleSpentAndReceivedRelation
import dev.ridill.oar.statistics.data.toBusiestTag
import dev.ridill.oar.statistics.data.toLargestSpend
import dev.ridill.oar.statistics.data.toTagSpend
import dev.ridill.oar.statistics.domain.model.BusiestTag
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.TagSpend
import dev.ridill.oar.statistics.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import java.util.Currency

internal class StatisticsRepositoryImpl(
    private val dao: AggregationsDao,
    private val cycleRepo: BudgetCycleRepository
) : StatisticsRepository {

    private fun getCycleBudget(cycleId: Long): Flow<Long> = cycleRepo
        .getCycleByIdFlow(cycleId)
        .mapLatest { it?.budget.orZero() }
        .distinctUntilChanged()

    override fun getCycleTotals(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<CycleSpentAndReceivedRelation?> = dao.getCycleTotals(
        cycleId = cycleId,
        currencyCode = currency.currencyCode,
        addExcluded = addExcluded
    )

    private fun getTransactionsSum(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean,
        movement: FundMovement
    ): Flow<Double> = dao.getTransactionsSum(
        cycleId = cycleId,
        currencyCode = currency.currencyCode,
        addExcluded = addExcluded,
        movement = movement
    )

    override fun getCreditsInclusiveBudget(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<Double> = combineTuple(
        getCycleBudget(cycleId),
        getTransactionsSum(cycleId, currency, addExcluded, FundMovement.IN),
    ).mapLatest { (
                      budget,
                      credits,
                  ) ->
        budget + credits
    }.distinctUntilChanged()

    override fun getTagSpends(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean,
        total: Double
    ): Flow<List<TagSpend>> = dao.getTagsWithAggregates(
        cycleId = cycleId,
        currencyCode = currency.currencyCode,
        addExcluded = addExcluded
    ).mapLatest { list ->
        list.map { it.toTagSpend(total) }
    }

    override fun getLargestSpend(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<LargestSpend?> = dao.getLargestSpendForCycle(
        cycleId = cycleId,
        currencyCode = currency.currencyCode,
        addExcluded = addExcluded
    ).mapLatest { relation -> relation?.toLargestSpend(currency) }
        .distinctUntilChanged()

    override fun getTransactionCount(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<Int> = dao.getTransactionCountForCycle(
        cycleId = cycleId,
        currencyCode = currency.currencyCode,
        addExcluded = addExcluded
    ).distinctUntilChanged()

    override fun getBusiestTag(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<BusiestTag?> = dao.getBusiestTagForCycle(
        cycleId = cycleId,
        currencyCode = currency.currencyCode,
        addExcluded = addExcluded
    ).mapLatest { it?.toBusiestTag() }
        .distinctUntilChanged()
}
