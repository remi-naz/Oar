package dev.ridill.oar.aggregations.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.statistics.data.local.relation.CycleAggregateRelation
import dev.ridill.oar.statistics.data.local.relation.CycleTotalsRelation
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
        SELECT
            IFNULL(SUM(CASE WHEN fundMovement = 'OUT' THEN transactionAmount END), 0) as spent,
            IFNULL(SUM(CASE WHEN fundMovement = 'IN' THEN transactionAmount END), 0) as received,
            COUNT(*) as transactionCount
        FROM transaction_details_view
        WHERE cycleId = :cycleId
            AND currencyCode = :currencyCode
            AND (:addExcluded = 1 OR excluded = 0)
    """
    )
    fun getCycleTotals(
        cycleId: Long,
        currencyCode: String,
        addExcluded: Boolean
    ): Flow<CycleTotalsRelation>

    @Query(
        """
        SELECT bc.id as id,
            bc.start_date as startDate,
            bc.end_date as endDate,
            bc.budget as budget,
            IFNULL(SUM(CASE WHEN tx.fundMovement = 'OUT' THEN tx.transactionAmount END), 0) as spent,
            IFNULL(SUM(CASE WHEN tx.fundMovement = 'IN' THEN tx.transactionAmount END), 0) as received,
            currency_code as currencyCode
        FROM budget_cycle_table bc
        LEFT OUTER JOIN transaction_details_view tx ON (
            tx.cycleId = bc.id
            AND tx.currencyCode = bc.currency_code
            AND (:addExcluded = 1 OR tx.excluded = 0)
        )
        WHERE bc.currency_code = :currencyCode
        GROUP BY bc.id
        ORDER BY DATE(bc.end_date) DESC, DATE(bc.start_date) DESC
        LIMIT :limit
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getCycleAggregatesGroupedByCycle(
        currencyCode: String,
        limit: Int,
        addExcluded: Boolean
    ): Flow<List<CycleAggregateRelation>>
}