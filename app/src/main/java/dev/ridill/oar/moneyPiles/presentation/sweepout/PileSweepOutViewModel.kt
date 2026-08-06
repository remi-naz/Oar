package dev.ridill.oar.moneyPiles.presentation.sweepout

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
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.navigation.PileSweepOutConfirmationSheetRoute
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.repository.PileSweepOutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PileSweepOutViewModel.Factory::class)
class PileSweepOutViewModel @AssistedInject constructor(
    @Assisted val route: PileSweepOutConfirmationSheetRoute,
    private val savedStateHandle: SavedStateHandle,
    private val repo: PileSweepOutRepository,
    private val eventBus: EventBus<PileSweepOutEvent>,
) : ViewModel(), PileSweepOutActions {

    @AssistedFactory
    interface Factory {
        fun create(route: PileSweepOutConfirmationSheetRoute): PileSweepOutViewModel
    }

    private val timestampNow = MutableStateFlow(DateUtil.now())
    private val _loading = MutableStateFlow(false)
    private val pile = repo.getPileDetails(route.pileId)
        .distinctUntilChanged()

    private val pileAggregate = repo.getSweepableAmount(route.pileId)
        .distinctUntilChanged()
    val sweepAmountInput = savedStateHandle.saveable(
        key = "SWEEP_AMOUNT_INPUT",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )
    private val amountInputParsed = sweepAmountInput.textAsFlow()
        .mapLatest { TextFormat.parseNumber(it) }
        .mapLatest { it.orZero() }
        .distinctUntilChanged()

    private val amountInputError = savedStateHandle
        .getStateFlow<UiText?>(AMOUNT_ERROR, null)

    private val createLinkedTransaction = savedStateHandle
        .getStateFlow(CREATE_LINKED_TX, false)

    private val previewAmount = combineTuple(
        amountInputParsed,
        pileAggregate
    ).mapLatest { (amount, limit) -> amount.coerceAtMost(limit) }
        .distinctUntilChanged()

    private val confirmEnabled = combineTuple(
        amountInputParsed,
        pileAggregate,
    ).mapLatest { (amount, limit) -> amount <= limit }
        .distinctUntilChanged()

    val state = combineTuple(
        timestampNow,
        _loading,
        pile,
        pileAggregate,
        amountInputError,
        createLinkedTransaction,
        previewAmount,
        confirmEnabled,
    ).mapLatest { (
                      timestampNow,
                      loading,
                      pile,
                      aggregate,
                      amountInputError,
                      createLinkedTransaction,
                      previewAmount,
                      confirmEnabled,
                  ) ->
        PileSweepOutState(
            timestampNow = timestampNow,
            loading = loading,
            pile = pile,
            maxLimit = aggregate,
            amountInputError = amountInputError,
            createLinkedTransaction = createLinkedTransaction,
            previewAmount = previewAmount,
            confirmEnabled = confirmEnabled,
        )
    }
        .onStart { refreshInputs() }
        .asStateFlow(viewModelScope, PileSweepOutState())

    val events = eventBus.eventFlow

    init {
        keepAmountErrorUpdated()
    }

    private fun keepAmountErrorUpdated() = viewModelScope.launch {
        combineTuple(
            amountInputParsed,
            pileAggregate,
        ).collectLatest { (
                              amount,
                              aggregate
                          ) ->

            savedStateHandle[AMOUNT_ERROR] = if (amount > aggregate) UiText
                .StringResource(R.string.error_amount_greater_than_pile_aggregate)
            else null
        }
    }

    private fun refreshInputs() {
        val currentText = sweepAmountInput.text
        if (currentText.isNotEmpty()) return

        viewModelScope.launch {
            val aggregate = pileAggregate.first()
            sweepAmountInput.setTextAndPlaceCursorAtEnd(
                TextFormat.number(
                    value = aggregate,
                    isGroupingUsed = false
                )
            )
            savedStateHandle[CREATE_LINKED_TX] = true
        }
    }

    override fun refreshTimestampNow() {
        timestampNow.update { DateUtil.now() }
    }

    override fun onCreateLinkedTransactionToggle(checked: Boolean) {
        savedStateHandle[CREATE_LINKED_TX] = checked
    }

    override fun onConfirm() {
        viewModelScope.launch {
            _loading.update { true }
            val result = repo.sweepOutPile(
                pileId = route.pileId,
                sweepAmount = amountInputParsed.first(),
                createLinkedTransaction = createLinkedTransaction.value,
            )
            _loading.update { false }
            when (result) {
                is Result.Error -> eventBus.send(PileSweepOutEvent.ShowUiMessage(result.message))
                is Result.Success -> eventBus.send(PileSweepOutEvent.PileSweptOut)
            }
        }
    }

    sealed interface PileSweepOutEvent {
        data class ShowUiMessage(val uiText: UiText) : PileSweepOutEvent
        data object PileSweptOut : PileSweepOutEvent
    }
}

private const val CREATE_LINKED_TX = "CREATE_LINKED_TX"
private const val AMOUNT_ERROR = "AMOUNT_ERROR"
