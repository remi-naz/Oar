package dev.ridill.oar.moneyPiles.data.local.view

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyPileTransactionDao : BaseDao<MoneyPileTransactionsEntity> {
    @RawQuery
    suspend fun getTransactionsInPilePagedRaw(query: RoomRawQuery): List<MoneyPileTransactionsEntity>

    @Query("UPDATE money_pile_transactions_table SET locked = 1 WHERE pile_id = :pileId AND locked = 0")
    suspend fun lockAllEntriesInPile(pileId: Long)

    @Query("UPDATE money_pile_transactions_table SET transaction_id = :transactionId WHERE id = :id")
    suspend fun setLinkedTransactionId(id: Long, transactionId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM money_pile_transactions_table WHERE pile_id = :pileId AND locked = 1 LIMIT 1)")
    fun doLockedEntriesExistForPile(pileId: Long): Flow<Boolean>

    @Query("DELETE FROM money_pile_transactions_table WHERE id = :id")
    suspend fun deleteById(id: Long)
}
