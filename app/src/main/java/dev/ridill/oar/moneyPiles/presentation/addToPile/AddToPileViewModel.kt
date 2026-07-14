package dev.ridill.oar.moneyPiles.presentation.addToPile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.navigation.AddToPileSheetRoute
import dev.ridill.oar.moneyPiles.domain.repository.AddToPileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel(assistedFactory = AddToPileViewModel.Factory::class)
class AddToPileViewModel @Inject constructor(
//    @Assisted val route: AddToPileSheetRoute,
//    savedStateHandle: SavedStateHandle,
//    private val repo: AddToPileRepository,
//    private val eventBus: EventBus<AddToPileEvent>
) : ViewModel(){

//    @AssistedFactory
//    interface Factory {
//        fun create(route: AddToPileSheetRoute): AddToPileViewModel
//    }

//    val pile = repo.getPileById(route.pileId)
//        .asStateFlow(viewModelScope, null)
//
//    val amountInputState = savedStateHandle.saveable(
//        key = "AMOUNT_INPUT_STATE",
//        saver = TextFieldState.Saver,
//        init = { TextFieldState() }
//    )
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading get() = _isLoading.asStateFlow()
//
//    val events = eventBus.eventFlow
//
//    override fun onConfirm() {
//        viewModelScope.launch {
//            val amount = amountInputState.text.toString().toDoubleOrNull()
//            if (amount == null || amount <= 0) return@launch
//
//            _isLoading.update { true }
//            repo.addToPile(route.pileId, amount, route.direction)
//            _isLoading.update { false }
//            eventBus.send(AddToPileEvent.Deposited)
//        }
//    }
//
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
