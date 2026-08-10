package dev.ridill.oar.moneyPiles.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(
    contentEntity = MoneyPileEntity::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    tokenizerArgs = ["remove_diacritics=2"]
)
@Entity(tableName = "money_pile_fts")
data class MoneyPileFtsEntity(
    @ColumnInfo(name = "name")
    val name: String
)
