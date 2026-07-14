package dev.ridill.oar.moneyPiles.presentation.addEditPile

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence

data class AddEditPileState(
    val isLoading: Boolean = false,
    val icon: PileIcon = PileIcon.EmergencyFund,
    val color: Color = Color.Unspecified,
    val contributionMode: PileContributionMode = PileContributionMode.FROM_BALANCE,
    val reminderCadence: PileReminderCadence = PileReminderCadence.NO_REPEAT,
    val reminderBehavior: PileReminderBehavior = PileReminderBehavior.REMIND,
    val showIconColorSelection: Boolean = false,
    val locked: Boolean = false,
    val errorMessage: UiText? = null,
)