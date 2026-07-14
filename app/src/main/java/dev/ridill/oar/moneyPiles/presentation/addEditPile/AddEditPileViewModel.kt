package dev.ridill.oar.moneyPiles.presentation.addEditPile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.navigation.AddEditPileRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AddEditPileViewModel.Factory::class)
class AddEditPileViewModel @AssistedInject constructor(
    @Assisted val route: AddEditPileRoute,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), AddEditPileActions {

    @AssistedFactory
    interface Factory {
        fun create(route: AddEditPileRoute): AddEditPileViewModel
    }

    val isEditMode = route.pileId != INVALID_ID_LONG

    private val _isLoading = MutableStateFlow(false)
    val isLoading get() = _isLoading.asStateFlow()

    val nameState = savedStateHandle.saveable(
        key = "NAME_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )
    val noteState = savedStateHandle.saveable(
        key = "NOTE_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )
    val starterAmountState = savedStateHandle.saveable(
        key = "STARTER_AMOUNT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )
    val targetAmountState = savedStateHandle.saveable(
        key = "TARGET_AMOUNT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )
    val reminderAmountState = savedStateHandle.saveable(
        key = "REMINDER_AMOUNT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val icon = savedStateHandle.getStateFlow(ICON, PileIcon.Savings)
    private val color = savedStateHandle.getStateFlow(COLOR, SelectableColorsList.random().toArgb())
    private val showIconColorSelection = savedStateHandle
        .getStateFlow(SHOW_ICON_COLOR_SELECTION, false)

    private val mode = savedStateHandle.getStateFlow(MODE, PileContributionMode.FROM_BALANCE)
    private val cadence = savedStateHandle.getStateFlow(CADENCE, PileReminderCadence.NO_REPEAT)
    private val behavior = savedStateHandle.getStateFlow(BEHAVIOR, PileReminderBehavior.REMIND)
    private val locked = savedStateHandle.getStateFlow(LOCKED, false)

    val state = combineTuple(
        icon,
        color,
        showIconColorSelection,
        mode,
        cadence,
        behavior,
        locked
    ).map { (
                icon,
                color,
                showIconColorSelection,
                mode,
                cadence,
                behavior,
                locked
            ) ->
        AddEditPileState(
            icon = icon,
            color = Color(color),
            showIconColorSelection = showIconColorSelection,
            contributionMode = mode,
            reminderCadence = cadence,
            reminderBehavior = behavior,
            locked = locked
        )
    }.asStateFlow(viewModelScope, AddEditPileState())

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
//        val pile = route.pileId
//            .takeIf { it != INVALID_ID_LONG }
//            ?.let { repo.getPileById(it) }
//            ?: MoneyPile.NEW
//        nameState.setTextAndPlaceCursorAtEnd(pile.name)
//        noteState.setTextAndPlaceCursorAtEnd(pile.note)
//        targetAmountState.setTextAndPlaceCursorAtEnd(pile.targetAmount?.toString().orEmpty())
//        reminderAmountState.setTextAndPlaceCursorAtEnd(
//            pile.reminderAmount.takeIf { it > 0 }?.toString().orEmpty()
//        )
    }

    override fun onIconIndicatorClick() {
        savedStateHandle[SHOW_ICON_COLOR_SELECTION] = true
    }

    override fun onIconColorSelectionDismiss() {
        savedStateHandle[SHOW_ICON_COLOR_SELECTION] = false
    }

    override fun onIconColorSelectionConfirm(
        icon: PileIcon,
        color: Color
    ) {
        savedStateHandle[ICON] = icon
        savedStateHandle[COLOR] = color.toArgb()
        savedStateHandle[SHOW_ICON_COLOR_SELECTION] = false
    }

    override fun onModeChange(mode: PileContributionMode) {
        savedStateHandle[MODE] = mode
    }

    override fun onCadenceChange(cadence: PileReminderCadence) {
        savedStateHandle[CADENCE] = cadence
    }

    override fun onBehaviorChange(behavior: PileReminderBehavior) {
        savedStateHandle[BEHAVIOR] = behavior
    }

    override fun onLockToggle() {
        savedStateHandle[LOCKED] = locked.value.not()
    }

    override fun onSaveClick() {
        viewModelScope.launch {
            val name = nameState.text.trim()
            if (name.isEmpty()) {
                savedStateHandle[ERROR_MESSAGE] = UiText.StringResource(
                    R.string.error_invalid_pile_name,
                    isErrorText = true
                )
                return@launch
            }

            _isLoading.update { true }

//            val target = targetAmountState.text.toString()
//                .toDoubleOrNull()
//                ?.takeIf { it > 0 }
//            val reminderAmount = reminderAmountState.text.toString().toDoubleOrNull().orZero()
//            val starterAmount = starterAmountState.text.toString().toDoubleOrNull().orZero()
//
//            val pileToSave = input.copy(
//                name = name.toString(),
//                note = noteState.text.toString(),
//                targetAmount = target,
//                reminderAmount = reminderAmount,
//                currentAmount = if (isEditMode) input.currentAmount else starterAmount
//            )
//
//            val savedId = repo.savePile(pileToSave)
            _isLoading.update { false }
//            eventBus.send(AddEditPileEvent.PileSaved(savedId))
        }
    }

    sealed interface AddEditPileEvent {
        data class PileSaved(val pileId: Long) : AddEditPileEvent
    }
}

private const val ICON = "ICON"
private const val COLOR = "COLOR"
private const val SHOW_ICON_COLOR_SELECTION = "SHOW_ICON_COLOR_SELECTION"
private const val MODE = "MODE"
private const val CADENCE = "CADENCE"
private const val BEHAVIOR = "BEHAVIOR"
private const val LOCKED = "LOCKED"
private const val ERROR_MESSAGE = "ERROR_MESSAGE"
