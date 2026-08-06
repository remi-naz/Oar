package dev.ridill.oar.moneyPiles.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MoneyPileDao : BaseDao<MoneyPileEntity> {
    @RawQuery
    suspend fun getMoneyPilesWithAggregatePagedRaw(query: RoomRawQuery): List<MoneyPileAggregateView>

    @Query("SELECT * FROM money_pile_table WHERE id = :id")
    suspend fun getPileById(id: Long): MoneyPileEntity?

    @Query("SELECT * FROM money_pile_table WHERE id = :id")
    fun getPileByIdFlow(id: Long): Flow<MoneyPileEntity?>

    @Query("UPDATE money_pile_table SET completion_timestamp = :timestamp WHERE id = :id")
    suspend fun setCompletedTimestampForPile(id: Long, timestamp: LocalDateTime)

    @Query("UPDATE money_pile_table SET target_remaining_amount = :amount WHERE id = :id")
    suspend fun setTargetRemainingAmountForPile(id: Long, amount: Double?)

    @Query("DELETE FROM money_pile_table WHERE id = :id")
    suspend fun deletePileById(id: Long)
}
