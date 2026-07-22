package dev.ridill.oar.moneyPiles.presentation.addToPile

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
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.navigation.AddToPileRoute
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.repository.AddToPileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AddToPileViewModel.Factory::class)
class AddToPileViewModel @AssistedInject constructor(
    @Assisted val route: AddToPileRoute,
    savedStateHandle: SavedStateHandle,
    private val repo: AddToPileRepository,
    private val eventBus: EventBus<AddToPileEvent>
) : ViewModel(), AddToPileActions {

    @AssistedFactory
    interface Factory {
        fun create(route: AddToPileRoute): AddToPileViewModel
    }

    private val pile = repo.getPileById(route.pileId)
        .asStateFlow(viewModelScope, null)

    val amountInputState = savedStateHandle.saveable(
        key = "AMOUNT_INPUT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val _isLoading = MutableStateFlow(false)
    private val addEnabled = amountInputState.textAsFlow()
        .mapLatest { TextFormat.parseNumber(it) != null }
        .distinctUntilChanged()
    val state = combineTuple(
        pile,
        _isLoading,
        addEnabled,
    ).mapLatest { (
                      pile,
                      loading,
                      addEnabled,
                  ) ->
        AddToPileState(
            pile = pile,
            loading = loading,
            addEnabled = addEnabled,
        )
    }.asStateFlow(viewModelScope, AddToPileState())

    val events = eventBus.eventFlow

    override fun onAddRecommendedAmountClick() {
        val amount = pile.value?.reminderAmount.orZero()
        amountInputState.setTextAndPlaceCursorAtEnd(TextFormat.number(amount))
    }

    override fun onConfirm() {
        viewModelScope.launch {
            val amount = TextFormat.parseNumber(amountInputState.text.toString())
            if (amount == null || amount <= 0) return@launch

            _isLoading.update { true }
            repo.addToPile(pileId = route.pileId, amount = amount, movement = route.movement)
            _isLoading.update { false }
            eventBus.send(AddToPileEvent.Deposited)
        }
    }

//    fun onQuickAmountClick(amount: Double) {
//        val current = amountInputState.text.toString().toDoubleOrNull() ?: 0.0
//        amountInputState.setTextAndPlaceCursorAtEnd((current + amount).toString())
//    }
//
//    fun onClearAmountClick() {
//        amountInputState.clearText()
//    }

    sealed interface AddToPileEvent {
        data object Deposited : AddToPileEvent
    }
}
