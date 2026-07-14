package dev.ridill.oar.moneyPiles.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.moneyPiles.domain.model.PileEntryDirection

@Entity(
    tableName = "money_pile_entry_table",
    foreignKeys = [
        ForeignKey(
            entity = MoneyPileEntity::class,
            parentColumns = ["id"],
            childColumns = ["money_pile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MoneyPileEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = OarDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "money_pile_id")
    val moneyPileId: Long,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "type")
    val type: PileEntryDirection,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
)
