package dev.ridill.oar.statistics.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import dev.ridill.oar.statistics.data.local.relation.CycleAggregateRelation
import dev.ridill.oar.statistics.data.local.relation.LargestSpendRelation
import dev.ridill.oar.statistics.data.local.relation.TagSpendRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query(
        """
        SELECT tagId, tagName, tagColorCode,
            SUM(transactionAmount) as amount,
            COUNT(*) as transactionCount
        FROM transaction_details_view
        WHERE cycleId = :cycleId
            AND currencyCode = :currencyCode
            AND fundMovement = 'OUT'
            AND (:addExcluded = 1 OR excluded = 0)
        GROUP BY tagId
        ORDER BY amount DESC
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getTagBreakdownForCycle(
        cycleId: Long,
        currencyCode: String,
        addExcluded: Boolean
    ): Flow<List<TagSpendRelation>>

    @Query(
        """
        SELECT transactionAmount as amount, transactionNote as note
        FROM transaction_details_view
        WHERE cycleId = :cycleId
            AND currencyCode = :currencyCode
            AND fundMovement = 'OUT'
            AND (:addExcluded = 1 OR excluded = 0)
        ORDER BY transactionAmount DESC
        LIMIT 1
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getLargestSpendForCycle(
        cycleId: Long,
        currencyCode: String,
        addExcluded: Boolean
    ): Flow<LargestSpendRelation?>
}
