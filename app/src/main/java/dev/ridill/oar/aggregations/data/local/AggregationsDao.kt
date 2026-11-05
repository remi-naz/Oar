package dev.ridill.oar.aggregations.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.statistics.data.local.relation.BusiestTagRelation
import dev.ridill.oar.statistics.data.local.relation.CycleSpentAndReceivedRelation
import dev.ridill.oar.statistics.data.local.relation.LargestSpendRelation
import dev.ridill.oar.statistics.data.local.relation.TagSpendRelation
import dev.ridill.oar.transactions.data.local.relation.AmountAndCurrencyRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface AggregationsDao {
    @Query(
        """
        SELECT currencyCode, IFNULL(SUM(
            CASE
                WHEN fundMovement = 'OUT' THEN transactionAmount
                WHEN fundMovement = 'IN' THEN -transactionAmount
            END
        ), 0) as amount
        FROM transaction_details_view
        WHERE (transactionId IN (:selectedTxIds))
            AND (:addExcluded = 1 OR excluded = 0)
        GROUP BY currencyCode
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getAggregatesForTransactionIds(
        selectedTxIds: Set<Long>,
        addExcluded: Boolean
    ): Flow<List<AmountAndCurrencyRelation>>

    @Query(
        """
        SELECT currencyCode, IFNULL(SUM(
            CASE
                WHEN fundMovement = 'OUT' THEN transactionAmount
                WHEN fundMovement = 'IN' THEN -transactionAmount
            END
        ), 0) as amount
        FROM transaction_details_view
        WHERE (cycleId = :cycleId)
            AND (:type IS NULL OR fundMovement = :type)
            AND (:currencyCode IS NULL OR currencyCode = :currencyCode)
            AND (:addExcluded = 1 OR excluded = 0)
        GROUP BY currencyCode
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getAggregatesForCycle(
        cycleId: Long,
        type: FundMovement?,
        currencyCode: String?,
        addExcluded: Boolean
    ): Flow<List<AmountAndCurrencyRelation>>

    @Query(
        """
         SELECT IFNULL(SUM(
            CASE
                WHEN type = 'OUT' THEN amount
                WHEN type = 'IN' THEN -amount
            END
        ), 0) as amount
        FROM transaction_table WHERE cycle_id = :cycleId
    """
    )
    suspend fun getAggregateAmountForCycle(cycleId: Long): Double

    @Query(
        """
         SELECT IFNULL(SUM(
            CASE
                WHEN movement = 'OUT' THEN -amount
                WHEN movement = 'IN' THEN amount
            END
        ), 0) as amount
        FROM money_pile_transactions_table
        WHERE pile_id = :pileId AND locked = 0
    """
    )
    fun getAggregateAmountForMoneyPileFlow(pileId: Long): Flow<Double>

    @Query(
        """
        SELECT
            IFNULL(SUM(CASE WHEN type = 'OUT' THEN amount END), 0) as spent,
            IFNULL(SUM(CASE WHEN type = 'IN' THEN amount END), 0) as received
        FROM transaction_table
        WHERE cycle_id = :cycleId
            AND currency_code = :currencyCode
            AND (:addExcluded = 1 OR is_excluded = 0)
    """
    )
    fun getCycleTotals(
        cycleId: Long,
        currencyCode: String,
        addExcluded: Boolean
    ): Flow<CycleSpentAndReceivedRelation>

    @Query(
        """
            SELECT IFNULL(SUM(amount), 0)
            FROM transaction_table
            WHERE cycle_id = :cycleId
            AND currency_code = :currencyCode
            AND type = :movement
            AND (:addExcluded = 1 OR is_excluded = 0)
        """
    )
    fun getTransactionsSum(
        cycleId: Long,
        currencyCode: String,
        addExcluded: Boolean,
        movement: FundMovement
    ): Flow<Double>

    @Query(
        """
        SELECT tagId, tagName, tagColorCode, IFNULL(SUM(transactionAmount), 0) as amount
        FROM transaction_details_view
        WHERE cycleId = :cycleId
            AND fundMovement = 'OUT'
            AND currencyCode = :currencyCode
            AND (:addExcluded = 1 OR excluded = 0)
        GROUP BY tagId
        ORDER BY amount DESC
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getTagsWithAggregates(
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

    @Query(
        """
        SELECT COUNT(DISTINCT transactionId)
        FROM transaction_details_view
        WHERE cycleId = :cycleId
            AND currencyCode = :currencyCode
            AND (:addExcluded = 1 OR excluded = 0)
    """
    )
    fun getTransactionCountForCycle(
        cycleId: Long,
        currencyCode: String,
        addExcluded: Boolean
    ): Flow<Int>

    @Query(
        """
        SELECT tagName, COUNT(DISTINCT transactionId) AS transactionCount
        FROM transaction_details_view
        WHERE cycleId = :cycleId
            AND currencyCode = :currencyCode
            AND tagId IS NOT NULL
            AND (:addExcluded = 1 OR excluded = 0)
        GROUP BY tagId
        ORDER BY transactionCount DESC
        LIMIT 1
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getBusiestTagForCycle(
        cycleId: Long,
        currencyCode: String,
        addExcluded: Boolean
    ): Flow<BusiestTagRelation?>
}
