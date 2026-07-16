package dev.ridill.oar.moneyPiles.data.local

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileEntity
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView

@Dao
interface MoneyPileDao : BaseDao<MoneyPileEntity> {
    @RawQuery
    suspend fun getMoneyPilesWithAggregatePagedRaw(query: RoomRawQuery): List<MoneyPileAggregateView>

    @RawQuery
    suspend fun getTransactionsInPilePagedRaw(query: RoomRawQuery): List<MoneyPileTransactionsEntity>
}
