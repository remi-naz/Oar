package dev.ridill.oar.moneyPiles.presentation.addEditPile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.navigation.AddEditPileRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.model.AddEditPileOption
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.moneyPiles.domain.repository.AddEditPileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Currency

@HiltViewModel(assistedFactory = AddEditPileViewModel.Factory::class)
class AddEditPileViewModel @AssistedInject constructor(
    @Assisted val route: AddEditPileRoute,
    private val savedStateHandle: SavedStateHandle,
    private val repo: AddEditPileRepository,
    private val cycleRepo: BudgetCycleRepository,
    private val eventBus: EventBus<AddEditPileEvent>,
) : ViewModel(), AddEditPileActions {

    @AssistedFactory
    interface Factory {
        fun create(route: AddEditPileRoute): AddEditPileViewModel
    }

    val isEditMode get() = route.pileId != INVALID_ID_LONG

    private val _isLoading = MutableStateFlow(false)
    val isLoading get() = _isLoading.asStateFlow()

    private val pileInput = savedStateHandle.getStateFlow<MoneyPileDetails?>(PILE_INPUT, null)
    val nameState = savedStateHandle.saveable(
        key = "NAME_STATE",
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

    private val icon = pileInput
        .mapLatest { it?.icon ?: PileIcon.LandProperty }
        .distinctUntilChanged()
    private val color = pileInput
        .mapLatest { it?.colorCode ?: SelectableColorsList.random().toArgb() }
    private val showIconColorSelection = savedStateHandle
        .getStateFlow(SHOW_ICON_COLOR_SELECTION, false)

    private val mode = pileInput
        .mapLatest { it?.contributionMode ?: PileContributionMode.FROM_BALANCE }
        .distinctUntilChanged()
    private val cadence = pileInput
        .mapLatest { it?.reminderCadence ?: PileReminderCadence.NO_REMIND }
        .distinctUntilChanged()
    private val behavior = pileInput
        .mapLatest { it?.reminderBehavior ?: PileReminderBehavior.REMIND }
        .distinctUntilChanged()
    private val locked = pileInput
        .mapLatest { it?.locked == true }
        .distinctUntilChanged()

    private val currency = pileInput
        .mapLatest {
            it?.currency
                ?: cycleRepo.getActiveCycle()?.currency
                ?: LocaleUtil.defaultCurrency
        }.distinctUntilChanged()

    private val menuOptions = savedStateHandle
        .getStateFlow<Set<AddEditPileOption>>(OPTIONS, emptySet())

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    val state = combineTuple(
        icon,
        color,
        showIconColorSelection,
        mode,
        cadence,
        behavior,
        locked,
        currency,
        menuOptions,
        showDeleteConfirmation,
    ).map { (
                icon,
                color,
                showIconColorSelection,
                mode,
                cadence,
                behavior,
                locked,
                currency,
                menuOptions,
                showDeleteConfirmation,
            ) ->
        AddEditPileState(
            icon = icon,
            color = Color(color),
            showIconColorSelection = showIconColorSelection,
            contributionMode = mode,
            reminderCadence = cadence,
            reminderBehavior = behavior,
            locked = locked,
            currency = currency,
            menuOptions = menuOptions,
            showDeleteConfirmation = showDeleteConfirmation,
        )
    }
        .onStart { buildOptions() }
        .asStateFlow(viewModelScope, AddEditPileState())

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() {
        if (pileInput.value != null) return
        viewModelScope.launch {
            val activeCycle = cycleRepo.getActiveCycle()
            val pile = repo.getPileById(route.pileId) ?: MoneyPileDetails.NEW.copy(
                currency = activeCycle?.currency ?: LocaleUtil.defaultCurrency
            )
            savedStateHandle[PILE_INPUT] = pile
            nameState.setTextAndPlaceCursorAtEnd(pile.name)
            targetAmountState.setTextAndPlaceCursorAtEnd(
                pile.targetAmount?.let(TextFormat::number).orEmpty()
            )
            reminderAmountState.setTextAndPlaceCursorAtEnd(
                pile.reminderAmount?.let(TextFormat::number).orEmpty()
            )
        }
    }

    private fun buildOptions() {
        savedStateHandle[OPTIONS] = AddEditPileOption.entries
            .takeIf { isEditMode }
            ?.toSet()
            .orEmpty()
    }

    override fun onCurrencySelect(currency: Currency) {
        savedStateHandle[PILE_INPUT] = pileInput.value?.copy(currency = currency)
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
        savedStateHandle[PILE_INPUT] = pileInput.value?.copy(
            icon = icon,
            colorCode = color.toArgb()
        )
        savedStateHandle[SHOW_ICON_COLOR_SELECTION] = false
    }

    override fun onModeChange(mode: PileContributionMode) {
        savedStateHandle[PILE_INPUT] = pileInput.value
            ?.copy(contributionMode = mode)
    }

    override fun onCadenceChange(cadence: PileReminderCadence) {
        savedStateHandle[PILE_INPUT] = pileInput.value
            ?.copy(reminderCadence = cadence)
    }

    override fun onBehaviorChange(behavior: PileReminderBehavior) {
        savedStateHandle[PILE_INPUT] = pileInput.value
            ?.copy(reminderBehavior = behavior)
    }

    override fun onLockToggle(value: Boolean) {
        savedStateHandle[PILE_INPUT] = pileInput.value
            ?.copy(locked = value)
    }

    override fun onSaveClick() {
        val input = pileInput.value ?: return
        viewModelScope.launch {
            val name = nameState.text.trim()
            if (name.isEmpty()) {
                eventBus.send(
                    AddEditPileEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_pile_name, isErrorText = true)
                    )
                )
                return@launch
            }

            _isLoading.update { true }

            val targetAmount = TextFormat
                .parseNumber(targetAmountState.text.toString())
            val reminderAmount = TextFormat
                .parseNumber(reminderAmountState.text.toString())
            val starterAmount = TextFormat
                .parseNumber(starterAmountState.text.toString())
                ?.takeIf { !isEditMode }
            val pileToSave = input.copy(
                name = name.toString(),
                targetAmount = targetAmount,
                reminderAmount = reminderAmount,
            )

            repo.savePile(pile = pileToSave, starterAmount = starterAmount)
            _isLoading.update { false }
            eventBus.send(AddEditPileEvent.PileSaved)
        }
    }

    override fun onOptionClick(option: AddEditPileOption) {
        when (option) {
            AddEditPileOption.DELETE -> {
                savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
            }
        }
    }

    override fun onDeleteConfirmationDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteConfirm() {
        viewModelScope.launch {
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            repo.deletePile(route.pileId)
            eventBus.send(AddEditPileEvent.PileDeleted)
        }
    }

    sealed interface AddEditPileEvent {
        data class ShowUiMessage(val text: UiText) : AddEditPileEvent
        data object PileSaved : AddEditPileEvent
        data object PileDeleted : AddEditPileEvent
    }
}

private const val PILE_INPUT = "PILE_INPUT"
private const val OPTIONS = "OPTIONS"
private const val SHOW_ICON_COLOR_SELECTION = "SHOW_ICON_COLOR_SELECTION"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
