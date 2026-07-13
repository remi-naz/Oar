package dev.ridill.oar.aggregations.data.repository

import dev.ridill.oar.aggregations.data.local.AggregationsDao
import dev.ridill.oar.aggregations.domain.repository.AggregationsRepository
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.transactions.data.local.relation.AmountAndCurrencyRelation
import dev.ridill.oar.transactions.data.toAggregateAmountItem
import dev.ridill.oar.transactions.domain.model.AggregateAmountItem
import dev.ridill.oar.core.domain.model.FundMovement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.util.Currency
import kotlin.math.absoluteValue

class AggregationsRepositoryImpl(
    private val dao: AggregationsDao
) : AggregationsRepository {
    override fun getAmountAggregateForTransactions(
        selectedTxIds: Set<Long>,
        addExcluded: Boolean
    ): Flow<List<AggregateAmountItem>> = dao.getAggregatesForTransactionIds(
        selectedTxIds = selectedTxIds,
        addExcluded = addExcluded
    ).mapLatest { it.map(AmountAndCurrencyRelation::toAggregateAmountItem) }
        .distinctUntilChanged()

    override fun getAmountAggregateForCycle(
        cycleId: Long,
        currency: Currency?,
        addExcluded: Boolean,
        type: FundMovement?
    ): Flow<List<AggregateAmountItem>> = dao.getAggregatesForCycle(
        cycleId = cycleId,
        addExcluded = addExcluded,
        type = type,
        currencyCode = currency?.currencyCode
    ).mapLatest { it.map(AmountAndCurrencyRelation::toAggregateAmountItem) }
        .distinctUntilChanged()

    override fun getTotalDebitsForCycle(
        id: Long,
        currency: Currency
    ): Flow<Double> = getAmountAggregateForCycle(
        cycleId = id,
        currency = currency,
        type = FundMovement.OUT,
        addExcluded = false,
    ).mapLatest { it.firstOrNull() }
        .mapLatest { it?.amount.orZero() }
        .mapLatest { it.absoluteValue }
        .distinctUntilChanged()

    override fun getTotalCreditsForCycle(
        id: Long,
        currency: Currency
    ): Flow<Double> = getAmountAggregateForCycle(
        cycleId = id,
        currency = currency,
        type = FundMovement.IN,
        addExcluded = false,
    ).mapLatest { it.firstOrNull() }
        .mapLatest { it?.amount.orZero() }
        .mapLatest { it.absoluteValue }
        .distinctUntilChanged()

    override suspend fun getAggregateAmountForCycle(id: Long): Double =
        withContext(Dispatchers.IO) {
            dao.getAggregateAmountForCycle(id)
        }
}