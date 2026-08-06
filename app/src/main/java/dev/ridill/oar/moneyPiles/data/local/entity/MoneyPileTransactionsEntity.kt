package dev.ridill.oar.moneyPiles.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.transactions.data.local.entity.TransactionEntity
import java.time.LocalDateTime

@Entity(
    tableName = "money_pile_transactions_table",
    foreignKeys = [
        ForeignKey(
            entity = MoneyPileEntity::class,
            parentColumns = ["id"],
            childColumns = ["pile_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.SET_NULL
        ),
    ],
    indices = [Index(value = ["pile_id", "created_timestamp"]), Index(value = ["transaction_id"]), Index(value = ["pile_id", "locked"])]
)
data class MoneyPileTransactionsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = OarDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "pile_id")
    val pileId: Long,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "movement")
    val movement: FundMovement,

    @ColumnInfo(name = "contribution_source")
    val contributionSource: ContributionSource,

    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: LocalDateTime,

    @ColumnInfo(name = "locked")
    val locked: Boolean = false,

    @ColumnInfo("transaction_id")
    val transactionId: Long?,
)
