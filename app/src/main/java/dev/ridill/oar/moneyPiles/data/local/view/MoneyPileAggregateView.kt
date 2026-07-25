package dev.ridill.oar.moneyPiles.data.local.view

import androidx.room.DatabaseView
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import java.time.LocalDate
import java.time.LocalDateTime

@DatabaseView(
    value = """
        SELECT 
        pile.id AS id,
        pile.name AS name,
        pile.icon AS icon,
        pile.color AS color,
        pile.contribution_mode AS contributionMode,
        pile.reminder_cadence AS reminderCadence,
        pile.reminder_behavior AS reminderBehavior,
        pile.locked AS locked,
        pile.currency_code AS currencyCode,
        pile.target_amount AS targetAmount,
        pile.target_date AS targetDate,
        pile.created_timestamp AS createdTimestamp,
        IFNULL(SUM(
            CASE
                WHEN tx.movement = 'OUT' THEN -tx.amount
                WHEN tx.movement = 'IN' THEN tx.amount
            END
        ), 0) as aggregate
        FROM money_pile_table pile
        LEFT JOIN money_pile_transactions_table tx ON pile.id = tx.pile_id
        GROUP BY pile.id
    """,
    viewName = "money_pile_aggregate_view"
)
data class MoneyPileAggregateView(
    val id: Long = OarDatabase.DEFAULT_ID_LONG,
    val name: String,
    val icon: PileIcon,
    val color: Int,
    val contributionMode: PileContributionMode,
    val reminderCadence: PileReminderCadence,
    val reminderBehavior: PileReminderBehavior,
    val locked: Boolean,
    val currencyCode: String,
    val targetAmount: Double?,
    val targetDate: LocalDate?,
    val createdTimestamp: LocalDateTime,
    val aggregate: Double,
)
