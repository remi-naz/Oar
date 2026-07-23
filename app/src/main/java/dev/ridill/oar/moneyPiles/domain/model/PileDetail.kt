package dev.ridill.oar.moneyPiles.domain.model

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.core.domain.util.Zero
import java.time.LocalDateTime
import java.util.Currency

data class PileDetail(
    val id: Long,
    val name: String,
    val icon: PileIcon,
    val color: Color,
    val currency: Currency,
    val contributionMode: PileContributionMode,
    val targetAmount: Double?,
    val savedAmount: Double,
    val locked: Boolean,
    val reminderCadence: PileReminderCadence,
    val reminderBehavior: PileReminderBehavior,
    val reminderAmount: Double?,
    val createdTimestamp: LocalDateTime,
) {
    val progressFraction: Float
        get() = if (targetAmount != null) (savedAmount / targetAmount).toFloat()
        else Float.Zero

    val isGoalReached: Boolean
        get() = targetAmount != null && savedAmount >= targetAmount
}
