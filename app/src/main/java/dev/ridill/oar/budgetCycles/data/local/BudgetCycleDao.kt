package dev.ridill.oar.budgetCycles.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import dev.ridill.oar.budgetCycles.data.local.entity.BudgetCycleEntity
import dev.ridill.oar.budgetCycles.data.local.view.BudgetCycleDetailsView
import dev.ridill.oar.budgetCycles.domain.model.CycleSelector
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.settings.data.local.ConfigKey
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BudgetCycleDao : BaseDao<BudgetCycleEntity> {

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM budget_cycle_details_view ORDER BY DATE(endDate) DESC LIMIT 1")
    suspend fun getLastCycle(): BudgetCycleDetailsView?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT id FROM budget_cycle_table")
    suspend fun getAllCycleIds(): List<Long>

    @Query("SELECT * FROM budget_cycle_details_view WHERE active = 1 LIMIT 1")
    fun getActiveCycleFlow(): Flow<BudgetCycleDetailsView?>

    @Query(
        """
        SELECT * 
        FROM budget_cycle_table
        WHERE id = (SELECT config_value FROM config_table WHERE config_key = '${ConfigKey.ACTIVE_CYCLE_ID}')
    """
    )
    suspend fun getActiveCycle(): BudgetCycleEntity?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM budget_cycle_details_view WHERE active = 1 LIMIT 1")
    suspend fun getActiveCycleDetailsView(): BudgetCycleDetailsView?

    @Query("SELECT * FROM budget_cycle_table WHERE DATE(start_date) = DATE(:startDate) AND DATE(end_date) = DATE(:endDate)")
    suspend fun getCycleForDate(
        startDate: LocalDate,
        endDate: LocalDate
    ): BudgetCycleEntity?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM budget_cycle_details_view WHERE id = :id")
    suspend fun getCycleById(id: Long): BudgetCycleDetailsView?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM budget_cycle_details_view WHERE id = :id")
    fun getCycleByIdFlow(id: Long): Flow<BudgetCycleDetailsView?>

    @Query(
        """
        UPDATE budget_cycle_table
        SET budget = :budget
        WHERE id = (SELECT config_value FROM config_table WHERE config_key = '${ConfigKey.ACTIVE_CYCLE_ID}')
        """
    )
    suspend fun updateBudgetForActiveCycle(budget: Long)

    @Query(
        """
        UPDATE budget_cycle_table
        SET currency_code = :currencyCode
        WHERE id = (SELECT config_value FROM config_table WHERE config_key = '${ConfigKey.ACTIVE_CYCLE_ID}')
        """
    )
    suspend fun updateCurrencyCodeForActiveCycle(currencyCode: String)

    @Query(
        """
        SELECT id, start_date AS startDate, end_date AS endDate 
        FROM budget_cycle_table
        ORDER BY start_date DESC, end_date DESC
        """
    )
    fun getCycleSelectionItems(): PagingSource<Int, CycleSelector>

    @Query(
        """
        SELECT * 
        FROM budget_cycle_details_view
        WHERE active = 0
        ORDER BY startDate DESC, endDate DESC
        """
    )
    fun getNotActiveCycleDetails(): PagingSource<Int, BudgetCycleDetailsView>
}