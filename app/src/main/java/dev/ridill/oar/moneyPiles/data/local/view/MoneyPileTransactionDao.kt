package dev.ridill.oar.moneyPiles.data.local.view

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity

@Dao
interface MoneyPileTransactionDao : BaseDao<MoneyPileTransactionsEntity> {
    @RawQuery
    suspend fun getTransactionsInPilePagedRaw(query: RoomRawQuery): List<MoneyPileTransactionsEntity>

    @Query("DELETE FROM money_pile_transactions_table WHERE id = :id")
    suspend fun deleteById(id: Long)
}
