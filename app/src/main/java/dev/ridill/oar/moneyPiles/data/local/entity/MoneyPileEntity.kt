package dev.ridill.oar.moneyPiles.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "money_pile_table",
    indices = [Index("name")]
)
data class MoneyPileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = OarDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: PileIcon,

    @ColumnInfo(name = "color")
    val color: Int,

    @ColumnInfo(name = "contribution_mode")
    val contributionMode: PileContributionMode,

    @ColumnInfo(name = "reminder_cadence")
    val reminderCadence: PileReminderCadence,

    @ColumnInfo(name = "reminder_behavior")
    val reminderBehavior: PileReminderBehavior,

    @ColumnInfo(name = "reminder_amount")
    val reminderAmount: Double?,

    @ColumnInfo(name = "locked")
    val locked: Boolean,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "target_amount")
    val targetAmount: Double?,

    @ColumnInfo(name = "target_date")
    val targetDate: LocalDate?,

    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: LocalDateTime,
)
