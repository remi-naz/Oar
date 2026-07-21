package dev.ridill.oar.moneyPiles.presentation.addEditPile

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.moneyPiles.domain.model.AddEditPileOption
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import java.util.Currency

interface AddEditPileActions {
    fun onCurrencySelect(currency: Currency)
    fun onIconIndicatorClick()
    fun onIconColorSelectionDismiss()
    fun onIconColorSelectionConfirm(icon: PileIcon, color: Color)
    fun onModeChange(mode: PileContributionMode)
    fun onCadenceChange(cadence: PileReminderCadence)
    fun onBehaviorChange(behavior: PileReminderBehavior)
    fun onLockToggle(value: Boolean)
    fun onSaveClick()
    fun onOptionClick(option: AddEditPileOption)
    fun onDeleteConfirmationDismiss()
    fun onDeleteConfirm()
}
