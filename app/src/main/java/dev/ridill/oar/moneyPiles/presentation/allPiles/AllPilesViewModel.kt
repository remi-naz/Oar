package dev.ridill.oar.moneyPiles.presentation.allPiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.ui.navigation.AddEditMoneyPileResult
import dev.ridill.oar.core.ui.navigation.PileFundMovementResult
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllPilesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    repo: AllPilesRepository,
    private val eventBus: EventBus<AllPilesEvent>,
) : ViewModel() {

    val includeCompletedPiles = savedStateHandle
        .getStateFlow(SHOW_COMPLETED, false)

    val pilesPagingData = includeCompletedPiles
        .flatMapLatest { includeLocked ->
            repo.getAllPilesPagedGroupedByCompleted(includeCompleted = includeLocked)
        }
        .cachedIn(viewModelScope)

    val events = eventBus.eventFlow

    fun onIncludeLockedPilesToggle(includeLocked: Boolean) {
        savedStateHandle[SHOW_COMPLETED] = includeLocked
    }

    fun onAddEditPileResult(result: AddEditMoneyPileResult) = viewModelScope.launch {
        val message = when (result) {
            AddEditMoneyPileResult.PILE_DELETED -> UiText.StringResource(R.string.pile_deleted)
            AddEditMoneyPileResult.PILE_SAVED -> UiText.StringResource(R.string.pile_saved)
        }

        eventBus.send(AllPilesEvent.ShowUiMessage(message))
    }

    fun onPileFundMovementResult(result: PileFundMovementResult) = viewModelScope.launch {
        eventBus.send(
            AllPilesEvent.ShowUiMessage(
                UiText.StringResource(
                    when (result) {
                        PileFundMovementResult.FUND_ADDED -> R.string.fund_added_to_pile
                        PileFundMovementResult.FUND_WITHDRAWN -> R.string.fund_withdrawn_from_pile
                    }
                )
            )
        )
    }

    sealed interface AllPilesEvent {
        data class ShowUiMessage(val text: UiText) : AllPilesEvent
    }
}

private const val SHOW_COMPLETED = "SHOW_COMPLETED"
