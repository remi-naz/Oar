package dev.ridill.oar.moneyPiles.data.local.view

import androidx.room.Dao
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity

@Dao
interface MoneyPileTransactionDao : BaseDao<MoneyPileTransactionsEntity> {
}
