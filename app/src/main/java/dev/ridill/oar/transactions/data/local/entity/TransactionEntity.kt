package dev.ridill.oar.transactions.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.ridill.oar.budgetCycles.data.local.entity.BudgetCycleEntity
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.folders.data.local.entity.FolderEntity
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import dev.ridill.oar.tags.data.local.entity.TagEntity
import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDateTime

@Entity(
    tableName = "transaction_table",
    foreignKeys = [
        ForeignKey(
            entity = BudgetCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycle_id"]
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"]
        ),
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"]
        ),
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("cycle_id"),
        Index("tag_id"),
        Index("folder_id"),
        Index("schedule_id"),
        Index("timestamp")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = OarDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: LocalDateTime,

    @ColumnInfo(name = "type")
    val type: FundMovement,

    @ColumnInfo(name = "is_excluded")
    val isExcluded: Boolean,

    @ColumnInfo("cycle_id")
    val cycleId: Long,

    @ColumnInfo(name = "tag_id")
    val tagId: Long?,

    @ColumnInfo(name = "folder_id")
    val folderId: Long?,

    @ColumnInfo(name = "schedule_id")
    val scheduleId: Long?
)