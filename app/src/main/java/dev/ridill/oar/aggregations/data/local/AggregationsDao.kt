package dev.ridill.oar.aggregations.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import dev.ridill.oar.transactions.data.local.relation.AmountAndCurrencyRelation
import dev.ridill.oar.core.domain.model.FundMovement
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
}