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
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.navigation.MoneyPileFundMovementRoute
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.repository.PileFundMovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovePileFundViewModel.Factory::class)
class MovePileFundViewModel @AssistedInject constructor(
    @Assisted val route: MoneyPileFundMovementRoute,
    savedStateHandle: SavedStateHandle,
    private val repo: PileFundMovementRepository,
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
        MovePileFundState(
            pile = pile,
            loading = loading,
            addEnabled = addEnabled,
        )
    }.asStateFlow(viewModelScope, MovePileFundState())

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
            repo.movePileFund(pileId = route.pileId, amount = amount, movement = route.movement)
            _isLoading.update { false }
            eventBus.send(MovePileFundEvent.FundMoved(movement = route.movement))
        }
    }

    sealed interface MovePileFundEvent {
        data class FundMoved(val movement: FundMovement) : MovePileFundEvent
    }
}
