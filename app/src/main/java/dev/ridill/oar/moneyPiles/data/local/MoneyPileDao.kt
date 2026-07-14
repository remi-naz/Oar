package dev.ridill.oar.moneyPiles.data.local

import androidx.room.Dao
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileEntity

@Dao
interface MoneyPileDao : BaseDao<MoneyPileEntity> {

}