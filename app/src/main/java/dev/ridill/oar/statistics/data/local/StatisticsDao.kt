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
        SELECT bc.id as id,
            bc.start_date as startDate,
            bc.end_date as endDate,
            bc.budget as budget,
            IFNULL(SUM(CASE WHEN tx.fundMovement = 'OUT' THEN tx.transactionAmount END), 0) as spent,
            IFNULL(SUM(CASE WHEN tx.fundMovement = 'IN' THEN tx.transactionAmount END), 0) as received
        FROM budget_cycle_table bc
        LEFT OUTER JOIN transaction_details_view tx ON (
            tx.cycleId = bc.id
            AND tx.currencyCode = bc.currency_code
            AND (:addExcluded = 1 OR tx.excluded = 0)
        )
        WHERE bc.currency_code = :currencyCode
        GROUP BY bc.id
        ORDER BY bc.start_date DESC, bc.end_date DESC
        LIMIT :limit
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getRecentCycleAggregates(
        currencyCode: String,
        limit: Int,
        addExcluded: Boolean
    ): Flow<List<CycleAggregateRelation>>

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
