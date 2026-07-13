package dev.ridill.oar.schedules.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDateTime

@Entity(tableName = "schedules_table")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = OarDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "type")
    val type: FundMovement,

    @ColumnInfo(name = "tag_id")
    val tagId: Long?,

    @ColumnInfo(name = "folder_id")
    val folderId: Long?,

    @ColumnInfo(name = "repetition")
    val repetition: ScheduleRepetition,

    @ColumnInfo(name = "last_payment_timestamp")
    val lastPaymentTimestamp: LocalDateTime?,

    @ColumnInfo(name = "next_payment_timestamp")
    val nextPaymentTimestamp: LocalDateTime?
)