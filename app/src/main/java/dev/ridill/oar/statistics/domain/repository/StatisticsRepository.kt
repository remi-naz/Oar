package dev.ridill.oar.statistics.domain.repository

import dev.ridill.oar.statistics.data.local.relation.CycleSpentAndReceivedRelation
import dev.ridill.oar.statistics.domain.model.BusiestTag
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.TagSpend
import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface StatisticsRepository {
    fun getCycleTotals(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<CycleSpentAndReceivedRelation?>

    fun getCreditsInclusiveBudget(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<Double>

    fun getTagSpends(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean,
        total: Double,
    ): Flow<List<TagSpend>>

    fun getLargestSpend(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<LargestSpend?>

    fun getTransactionCount(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<Int>

    fun getBusiestTag(
        cycleId: Long,
        currency: Currency,
        addExcluded: Boolean
    ): Flow<BusiestTag?>
}
