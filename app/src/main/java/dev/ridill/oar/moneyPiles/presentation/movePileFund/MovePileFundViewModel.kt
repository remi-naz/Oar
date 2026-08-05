package dev.ridill.oar.moneyPiles.presentation.movePileFund

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.navigation.MoneyPileFundMovementRoute
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.repository.PileFundMovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneOffset

@HiltViewModel(assistedFactory = MovePileFundViewModel.Factory::class)
class MovePileFundViewModel @AssistedInject constructor(
    @Assisted val route: MoneyPileFundMovementRoute,
    private val savedStateHandle: SavedStateHandle,
    private val repo: PileFundMovementRepository,
    private val cycleRepo: BudgetCycleRepository,
    private val eventBus: EventBus<MovePileFundEvent>
) : ViewModel(), MovePileFundActions {

    @AssistedFactory
    interface Factory {
        fun create(route: MoneyPileFundMovementRoute): MovePileFundViewModel
    }

    private val pile = repo.getPileById(route.pileId)
        .asStateFlow(viewModelScope, null)

    val amountInputState = savedStateHandle.saveable(
        key = "AMOUNT_INPUT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val _isLoading = MutableStateFlow(false)

    private val timestamp = savedStateHandle.getStateFlow(TIMESTAMP, DateUtil.now())
    private val showDatePicker = savedStateHandle.getStateFlow(SHOW_DATE_PICKER, false)
    private val showTimePicker = savedStateHandle.getStateFlow(SHOW_TIME_PICKER, false)

    private val selectedCycleId = savedStateHandle.getStateFlow<Long?>(SELECTED_CYCLE_ID, null)
    private val cycleDescription = selectedCycleId
        .flatMapLatest { id ->
            id?.let(cycleRepo::getCycleByIdFlow) ?: flowOf(null)
        }
        .mapLatest { it?.description }
        .distinctUntilChanged()

    val state = combineTuple(
        pile,
        _isLoading,
        timestamp,
        showDatePicker,
        showTimePicker,
        cycleDescription,
        selectedCycleId,
    ).mapLatest { (
                      pile,
                      loading,
                      timestamp,
                      showDatePicker,
                      showTimePicker,
                      cycleDescription,
                      selectedCycleId,
                  ) ->
        MovePileFundState(
            pile = pile,
            loading = loading,
            timestamp = timestamp,
            showDatePicker = showDatePicker,
            showTimePicker = showTimePicker,
            cycleDescription = cycleDescription,
            selectedCycleId = selectedCycleId,
        )
    }.asStateFlow(viewModelScope, MovePileFundState())

    val events = eventBus.eventFlow

    init {
        viewModelScope.launch {
            if (selectedCycleId.value == null) {
                savedStateHandle[SELECTED_CYCLE_ID] = cycleRepo.getActiveCycle()?.id
            }
        }
    }

    override fun onAddRecommendedAmountClick() {
        val amount = pile.value?.reminderAmount.orZero()
        amountInputState.setTextAndPlaceCursorAtEnd(TextFormat.number(amount))
    }

    fun onCycleSelect(id: Long?) {
        if (id == null) return
        savedStateHandle[SELECTED_CYCLE_ID] = id
    }

    override fun onTimestampClick() {
        savedStateHandle[SHOW_DATE_PICKER] = true
    }

    override fun onDateSelectionDismiss() {
        savedStateHandle[SHOW_DATE_PICKER] = false
    }

    override fun onDateSelectionConfirm(millis: Long) {
        savedStateHandle[TIMESTAMP] = DateUtil.dateFromMillisWithTime(
            millis = millis,
            time = timestamp.value.toLocalTime(),
            zoneId = ZoneOffset.UTC
        )
        savedStateHandle[SHOW_DATE_PICKER] = false
    }

    override fun onPickTimeClick() {
        savedStateHandle[SHOW_DATE_PICKER] = false
        savedStateHandle[SHOW_TIME_PICKER] = true
    }

    override fun onTimeSelectionDismiss() {
        savedStateHandle[SHOW_TIME_PICKER] = false
    }

    override fun onTimeSelectionConfirm(hour: Int, minute: Int) {
        savedStateHandle[TIMESTAMP] = timestamp.value
            .withHour(hour)
            .withMinute(minute)
        savedStateHandle[SHOW_TIME_PICKER] = false
    }

    override fun onPickDateClick() {
        savedStateHandle[SHOW_TIME_PICKER] = false
        savedStateHandle[SHOW_DATE_PICKER] = true
    }

    override fun onConfirm() {
        viewModelScope.launch {
            val amountInput = amountInputState.text.trim().toString()
            if (amountInput.isEmpty()) {
                eventBus.send(
                    MovePileFundEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }
            val amount = TextFormat.parseNumber(amountInput) ?: -1.0
            if (amount <= Double.Zero) {
                eventBus.send(
                    MovePileFundEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }

            val cycleId = selectedCycleId.value
            if (cycleId == null) {
                eventBus.send(
                    MovePileFundEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_no_cycle_selected, isErrorText = true)
                    )
                )
                return@launch
            }

            _isLoading.update { true }

            val result = repo.movePileFund(
                pileId = route.pileId,
                amount = amount,
                movement = route.movement,
                timestamp = timestamp.value,
                cycleId = cycleId
            )

            _isLoading.update { false }
            when (result) {
                is Result.Error -> {
                    eventBus.send(MovePileFundEvent.ShowUiMessage(result.message))
                }

                is Result.Success -> {
                    eventBus.send(MovePileFundEvent.FundMoved(movement = route.movement))
                }
            }
        }
    }

    sealed interface MovePileFundEvent {
        data class ShowUiMessage(val uiText: UiText) : MovePileFundEvent
        data class FundMoved(val movement: FundMovement) : MovePileFundEvent
    }
}

private const val TIMESTAMP = "TIMESTAMP"
private const val SHOW_DATE_PICKER = "SHOW_DATE_PICKER"
private const val SHOW_TIME_PICKER = "SHOW_TIME_PICKER"
private const val SELECTED_CYCLE_ID = "SELECTED_CYCLE_ID"
